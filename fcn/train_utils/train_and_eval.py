import torch
from torch import nn
import train_utils.distributed_utils as utils


def criterion(inputs, target):
    losses = {}
    for name, x in inputs.items():      # 遍历模型预测结果[ 输出为字典形式 ]
        # 忽略target中值为255的像素，255的像素是目标边缘或者padding填充
        losses[name] = nn.functional.cross_entropy(x, target, ignore_index=255)         # 计算损失[ x-网络预测结果，target-真实标签，target像素值为255忽略 ],

    if len(losses) == 1:                # 未使用辅助分类器，返回主输出
        return losses['out']

    return losses['out'] + 0.5 * losses['aux']      # 使用辅助分类器，综合返回


''' 训练完的验证过程 '''
def evaluate(model, data_loader, device, num_classes):
    model.eval()                # 开启模型的验证模式
    confmat = utils.ConfusionMatrix(num_classes)
    metric_logger = utils.MetricLogger(delimiter="  ")
    header = 'Test:'
    with torch.no_grad():               # 验证时不需要反向传播，故梯度可以不管 
        for image, target in metric_logger.log_every(data_loader, 100, header):
            image, target = image.to(device), target.to(device)
            output = model(image)
            output = output['out']      # 只使用主分支的输出
            # target 展平，对于输出 output - (batch_size,channel,W,H) .argmax(1) 找出 channel 中最大的值，定为类别
            # 计算混淆矩阵
            confmat.update(target.flatten(), output.argmax(1).flatten())

        confmat.reduce_from_all_processes()

    return confmat


''' 训练一轮的过程 '''
def train_one_epoch(model, optimizer, data_loader, device, epoch, lr_scheduler, print_freq=10, scaler=None):
    model.train()           # 开启模型的训练模式, Dropout() 和 Batch_Normalization() 被启用

    # 记录模型训练过程中的指标???[ self-defination ]
    metric_logger = utils.MetricLogger(delimiter="  ")      # 创建 MetricLogger 对象
    metric_logger.add_meter('lr', utils.SmoothedValue(window_size=1, fmt='{value:.6f}'))
    header = 'Epoch: [{}]'.format(epoch)

    # 从 data_loader 获取 image 和 target
    for image, target in metric_logger.log_every(data_loader, print_freq, header):
        image, target = image.to(device), target.to(device)             # 图像输入到设备中
        with torch.cuda.amp.autocast(enabled=scaler is not None):
            output = model(image)                                       # 图像输入模型中
            loss = criterion(output, target)                            # 计算损失值

        optimizer.zero_grad()       # 清空历史梯度
        if scaler is not None:
            scaler.scale(loss).backward()
            scaler.step(optimizer)
            scaler.update()
        else:
            loss.backward()         # 反向传播
            optimizer.step()        # 更新参数

        lr_scheduler.step()         # 更新学习率 - 每次迭代一个 step 即更新学习率

        lr = optimizer.param_groups[0]["lr"]        # 提取学习率
        metric_logger.update(loss=loss.item(), lr=lr)

    return metric_logger.meters["loss"].global_avg, lr      # 返回训练平均损失、学习率


''' 学习率更新策略 '''
def create_lr_scheduler(optimizer,
                        num_step: int,
                        epochs: int,
                        warmup=True,
                        warmup_epochs=1,
                        warmup_factor=1e-3):
    assert num_step > 0 and epochs > 0
    if warmup is False:
        warmup_epochs = 0

    def f(x):
        """
        根据step数返回一个学习率倍率因子，
        注意在训练开始之前，pytorch会提前调用一次lr_scheduler.step()方法
        """
        if warmup is True and x <= (warmup_epochs * num_step):
            alpha = float(x) / (warmup_epochs * num_step)
            # warmup过程中lr倍率因子从warmup_factor -> 1
            return warmup_factor * (1 - alpha) + alpha
        else:
            # warmup后lr倍率因子从1 -> 0
            # 参考deeplab_v2: Learning rate policy
            return (1 - (x - warmup_epochs * num_step) / ((epochs - warmup_epochs) * num_step)) ** 0.9

    return torch.optim.lr_scheduler.LambdaLR(optimizer, lr_lambda=f)
