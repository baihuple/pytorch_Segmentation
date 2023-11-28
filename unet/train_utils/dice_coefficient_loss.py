import torch
import torch.nn as nn


def build_target(target: torch.Tensor, num_classes: int = 2, ignore_index: int = -100):
    """build target for dice coefficient"""
    dice_target = target.clone()    # target_size - (4,480,480)

    # 边缘不考虑因素-255
    # 找到并且替换成 0
    # 经过 .one_hot() - dice_target.size() - (4,480,480,2)  # 针对每一个类别的 gt
    if ignore_index >= 0:
        ignore_mask = torch.eq(target, ignore_index)
        dice_target[ignore_mask] = 0
    
        # .one_hot() 方法：将原来 classes=2 的单通道结果，转化为两个Tensor，对应位置组合成一个向量，表示标签
        # (1,0) - 前景；(0,1) - 背景
        dice_target = nn.functional.one_hot(dice_target, num_classes).float()
        dice_target[ignore_mask] = ignore_index
    else:
        dice_target = nn.functional.one_hot(dice_target, num_classes).float()

    # [N, H, W] -> [N, H, W, C]，更改维度排列
    return dice_target.permute(0, 3, 1, 2)

''' 计算 dice 损失(3) '''
# x、target - size - (4,480,480)
def dice_coeff(x: torch.Tensor, target: torch.Tensor, ignore_index: int = -100, epsilon=1e-6):
    # Average of Dice coefficient for all batches, or for a single mask
    # 计算一个batch中所有图片某个类别的dice_coefficient
    d = 0.
    batch_size = x.shape[0]
    for i in range(batch_size):
        x_i = x[i].reshape(-1)          # X_i: 当前 batch 中第 i 张图片对应某一类别的预测概率矩阵 -- .reshape(-1)变成向量
        t_i = target[i].reshape(-1)     # t_i: 向量形式
        if ignore_index >= 0:
            # 找出mask中不为ignore_index的区域[ find interesting zone ]
            roi_mask = torch.ne(t_i, ignore_index)
            # 提取出感兴趣区域
            x_i = x_i[roi_mask]
            t_i = t_i[roi_mask]

        # 向量内积操作:相乘求和
        inter = torch.dot(x_i, t_i)                         # 计算分子
        sets_sum = torch.sum(x_i) + torch.sum(t_i)          # 计算分母

        # 全预测为 0，则计算得到的 dice = 1
        if sets_sum == 0:                                   # 即 x_i == t_i == 0
            sets_sum = 2 * inter

        d += (2 * inter + epsilon) / (sets_sum + epsilon)   

    # 返回每张图片针对每个类别的 dic_eff
    return d / batch_size

''' 计算 dice 损失(2) '''
# x、target - (4,2,480,480)
def multiclass_dice_coeff(x: torch.Tensor, target: torch.Tensor, ignore_index: int = -100, epsilon=1e-6):
    """Average of Dice coefficient for all classes"""
    dice = 0.
    # 遍历每一个 channel 的预测值和 target
    for channel in range(x.shape[1]):
        dice += dice_coeff(x[:, channel, ...], target[:, channel, ...], ignore_index, epsilon)

    # 返回所有类别的 dice_coeff 的均值
    return dice / x.shape[1]



''' 计算 dice 损失(1)'''
def dice_loss(x: torch.Tensor, target: torch.Tensor, multiclass: bool = False, ignore_index: int = -100):
    # Dice loss (objective to minimize) between 0 and 1
    # 对于 model 的 output 在 channel 方向进行 softmax 处理,像素中的值 ---> 类别概率
    x = nn.functional.softmax(x, dim=1)
    fn = multiclass_dice_coeff if multiclass else dice_coeff    
    return 1 - fn(x, target, ignore_index=ignore_index)         # fn - dice, 1 - fn :dice_loss
