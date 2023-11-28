import os
import time
import datetime
import torch
from src import fcn_resnet50,fcn_resnet101
from train_utils import train_one_epoch, evaluate, create_lr_scheduler
from my_dataset import VOCSegmentation
import transforms as T

''' 训练集中的图像预处理方法 - ouput: [480 , 480], 因为 crop_size 为 480, 在.RandomCrop() 定型 '''
class SegmentationPresetTrain:
    def __init__(self, base_size, crop_size, hflip_prob=0.5, mean=(0.485, 0.456, 0.406), std=(0.229, 0.224, 0.225)):        # hflip_prob - 水平翻转概率, mean/std - 标准化处理的均值和方差
        # 将图片大小进行重塑，最小边等比例缩放为这个随机值
        min_size = int(0.5 * base_size)
        max_size = int(2.0 * base_size)
        trans = [T.RandomResize(min_size, max_size)]        
        # 随机水平翻转概率,对 image、target 进行随机翻转 
        if hflip_prob > 0:                                     
            trans.append(T.RandomHorizontalFlip(hflip_prob))    
        trans.extend([
            T.RandomCrop(crop_size),                        # 随机裁剪图片到 480*480 大小
            T.ToTensor(),                                   # 图片转化为 tensor 格式
            T.Normalize(mean=mean, std=std),                # 标准化处理
        ])
        self.transforms = T.Compose(trans)                  # 将所有的图形变换汇总

    def __call__(self, img, target):
        return self.transforms(img, target)                 # 进行预处理


''' 对于验证集的预处理方法 '''
class SegmentationPresetEval:
    def __init__(self, base_size, mean=(0.485, 0.456, 0.406), std=(0.229, 0.224, 0.225)):
        self.transforms = T.Compose([
            T.RandomResize(base_size, base_size),
            T.ToTensor(),
            T.Normalize(mean=mean, std=std),
        ])

    def __call__(self, img, target):
        return self.transforms(img, target)


''' 若 train = true, 返回针对训练集的预处理方法 - 否则返回测试机的预处理方法'''
def get_transform(train):
    base_size = 520
    crop_size = 480
    return SegmentationPresetTrain(base_size, crop_size) if train else SegmentationPresetEval(base_size)


''' 创建模型 '''
''' aux - 是否使用辅助参数器, pretrain - 是否使用预训练权重 '''
def create_model(aux, num_classes, pretrain=True):
    # 使用 fcn_restnet 50 创建模型
    model = fcn_resnet50(aux=aux, num_classes=num_classes)  
    # 使用 fcn_restnet 101 创建模型，记得载入权重 torch.load() 要重新下载 101 权重
    # model = fcn_resnet101(aux=aux, num_classes=num_classes)      

    if pretrain:    # 若使用预训练权重
        weights_dict = torch.load("./fcn_resnet50_coco.pth", map_location='cpu') # 载入权重文件到 cpu 中，为字典形式 <尚未载入到模型中>
        
        # 官方提供的预训练权重是21类(包括背景)
        # 如果训练自己的数据集，将和类别相关的权重删除，防止权重shape不一致报错
        if num_classes != 21:      
            for k in list(weights_dict.keys()):
                if "classifier.4" in k:
                    del weights_dict[k]

        # 权重载入到模型当中 .load_state_dict(xxxx)
        missing_keys, unexpected_keys = model.load_state_dict(weights_dict, strict=False)       
        if len(missing_keys) != 0 or len(unexpected_keys) != 0:
            print("missing_keys: ", missing_keys)           # 输出 model 中尚未载入的权重
            print("unexpected_keys: ", unexpected_keys)     # 输出 model 中没有用到的权重

    return model


''' 主函数 '''
''' args 参数为 parse_args() 返回的参数 '''
def main(args):

    # 1.指定设备并初始化相关信息
    device = torch.device(args.device if torch.cuda.is_available() else "cpu")      # GPU 可用 - yes：默认使用第一块 GPU 设备 ? no: CPU
    batch_size = args.batch_size
    num_classes = args.num_classes + 1                                              # segmentation nun_classes(20) + background(1)


    # 2.创建.txt 文件，用来保存训练过程中，每一个 epoch 验证过程中的输出信息
    results_file = "results{}.txt".format(datetime.datetime.now().strftime("%Y%m%d-%H%M%S"))


    # 3.构造载入模型的数据 xxx_loader - 同时构造训练数据集(image / target)、预测数据集(image / target)
    # 调用 my_dataset 下的自定义数据集 VOCSegmentation
    # VOCdevkit -> VOC2012 -> ImageSets -> Segmentation -> train.txt
    train_dataset = VOCSegmentation(args.data_path,
                                    year="2012",
                                    transforms=get_transform(train=True),   # 训练集部分 transform: train=True / train.txt
                                    txt_name="train.txt")       

    # VOCdevkit -> VOC2012 -> ImageSets -> Segmentation -> val.txt  
    val_dataset = VOCSegmentation(args.data_path,
                                  year="2012",
                                  transforms=get_transform(train=False),    # 测试机部分 transform: train=False / val.txt
                                  txt_name="val.txt")
    
    num_workers = min([os.cpu_count(), batch_size if batch_size > 1 else 0, 8])
    train_loader = torch.utils.data.DataLoader(train_dataset,
                                               batch_size=batch_size,
                                               num_workers=num_workers,
                                               shuffle=True,
                                               pin_memory=True,              # pin_memory - 表示将 load 进的数据拷贝进锁内存区，将内存中的 Tensor 转移至 GPU cuda 区会很快 - https://zhuanlan.zhihu.com/p/483627709
                                               collate_fn=train_dataset.collate_fn) # collate_fn - 将样本整理成批次，Torch 中可以自定义整理

    val_loader = torch.utils.data.DataLoader(val_dataset,
                                             batch_size=1,
                                             num_workers=num_workers,
                                             pin_memory=True,
                                             collate_fn=val_dataset.collate_fn)


    # 4.创建模型，载入到 GPU 当中
    model = create_model(aux=args.aux, num_classes=num_classes)
    model.to(device)


    # 5.权重参数处理
    # 5.1) 提取相关参数
    # 结合 ./torch_fcn.png 来看
    # 将 backbone - (ResNet50) 和 classifier - (FCNHead) 所有的权重，把没有冻结的权重全部提取，等待一会训练
    params_to_optimize = [
        {"params": [p for p in model.backbone.parameters() if p.requires_grad]},
        {"params": [p for p in model.classifier.parameters() if p.requires_grad]}
    ]
    # 若启用 .aux 辅助分类器，aux_classifier 权重则提取，并将权重参数 .append() 添加到 params_to_optimize 中
    if args.aux:
        params = [p for p in model.aux_classifier.parameters() if p.requires_grad]
        params_to_optimize.append({"params": params, "lr": args.lr * 10})   

    # 5.2) 定义优化器 SGD
    optimizer = torch.optim.SGD(
        params_to_optimize,         # 传入训练参数
        lr=args.lr, momentum=args.momentum, weight_decay=args.weight_decay  # 设置优化器相关初始值
    )
    # 指定混合精度训练
    scaler = torch.cuda.amp.GradScaler() if args.amp else None
    # 5.3) 创建学习率更新策略，这里是每个 step 更新一次(不是每个epoch)，warmup 热身训练< 小学习率 --> 初始化学习率 --> 下降 >
    lr_scheduler = create_lr_scheduler(optimizer, len(train_loader), args.epochs, warmup=True)

    # *** resume - True：载入最近一次模型权重、优化器数据、学习率更新策略
    # .pth 保存预训练权重，.load 载入的信息为字典形式
    # torch.load() - https://blog.csdn.net/leviopku/article/details/123925804 && https://zhuanlan.zhihu.com/p/82038049
    # 下面为加载和保存一个通用的检查点( Checkpoint )，需要保存更多信息，optimizer、epoch、lr_scheduler 等等
    if args.resume:
        checkpoint = torch.load(args.resume, map_location='cpu')    
        model.load_state_dict(checkpoint['model'])                  # 载入对应模型权重
        optimizer.load_state_dict(checkpoint['optimizer'])          # 载入优化器的数据
        lr_scheduler.load_state_dict(checkpoint['lr_scheduler'])    # 载入学习率更新策略的数据
        args.start_epoch = checkpoint['epoch'] + 1                  # 载入训练轮数
        if args.amp:
            scaler.load_state_dict(checkpoint["scaler"])

    # 6.正式训练过程
    start_time = time.time()    # 获取时间戳(1970.1.1 ~ 现在的秒数) - https://blog.csdn.net/weixin_35757531/article/details/129074115
    # args.start_epoch - 当前轮数，args.epochs - 共需训练几轮
    for epoch in range(args.start_epoch, args.epochs):
         # 6.1) train_one_epoch() 训练一轮的过程
        mean_loss, lr = train_one_epoch(model, optimizer, train_loader, device, epoch,     
                                        lr_scheduler=lr_scheduler, print_freq=args.print_freq, scaler=scaler)
        
        # 6.2) 验证过程[ 目的：对当前1个epoch的FCN网络参数训练效果进行一个检验 ]
        # 获得验证集所有的混淆矩阵 confmat 
        confmat = evaluate(model, val_loader, device=device, num_classes=num_classes)
        val_info = str(confmat)     # 调用 distributed_utils.py 中的 __str__ 方法
        print(val_info)

        # 6.3) 在 requests_xxx_xxx.txt 进行记录
        with open(results_file, "a") as f:
            # 记录每个epoch对应的train_loss、lr以及验证集各指标
            train_info = f"[epoch: {epoch}]\n" \
                         f"train_loss: {mean_loss:.4f}\n" \
                         f"lr: {lr:.6f}\n"
            f.write(train_info + val_info + "\n\n")

        # 6.4) 保存模型参数 torch.save()
        save_file = {"model": model.state_dict(),
                     "optimizer": optimizer.state_dict(),
                     "lr_scheduler": lr_scheduler.state_dict(),
                     "epoch": epoch,
                     "args": args}
        if args.amp:
            save_file["scaler"] = scaler.state_dict()
        torch.save(save_file, "save_weights/model_{}.pth".format(epoch))

    total_time = time.time() - start_time
    total_time_str = str(datetime.timedelta(seconds=int(total_time)))
    print("training time {}".format(total_time_str))



''' 指定相关参数 '''
def parse_args():
    import argparse
    parser = argparse.ArgumentParser(description="pytorch fcn training")
    # parser.add_argument("--data-path", default="/data/", help="VOCdevkit root")            # PASCAL VOC 数据集路径
    parser.add_argument("--data-path", default="D:/FCN-relate/", help="VOCdevkit root")      # PASCAL VOC 数据集路径
    parser.add_argument("--num-classes", default=20, type=int)                               # 不包含背景类别数
    parser.add_argument("--aux", default=True, type=bool, help="auxilier loss")              # 辅助分类器（见图）
    parser.add_argument("--device", default="cuda", help="training device")
    parser.add_argument("-b", "--batch-size", default=4, type=int)
    # parser.add_argument("--epochs", default=30, type=int, metavar="N",                       # 训练多少轮
    #                     help="number of total epochs to train")
    parser.add_argument("--epochs", default=4, type=int, metavar="N",                       # 训练多少轮
                        help="number of total epochs to train")
    parser.add_argument('--lr', default=0.0001, type=float, help='initial learning rate')    # 初始学习率
    parser.add_argument('--momentum', default=0.9, type=float, metavar='M',                  # 优化器参数
                        help='momentum')
    parser.add_argument('--wd', '--weight-decay', default=1e-4, type=float,                  # 优化器参数
                        metavar='W', help='weight decay (default: 1e-4)',   
                        dest='weight_decay')
    parser.add_argument('--print-freq', default=10, type=int, help='print frequency')
    parser.add_argument('--resume', default='', help='resume from checkpoint')               # 若训练网络时 【eg：第n个epoch时程序中断，载入最近一次权重，default 指向最近一次保存的权重文件】
    parser.add_argument('--start-epoch', default=0, type=int, metavar='N',
                        help='start epoch')
    # Mixed precision training parameters
    parser.add_argument("--amp", default=False, type=bool,
                        help="Use torch.cuda.amp for mixed precision training")
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    args = parse_args()

    if not os.path.exists("./save_weights"):
        os.mkdir("./save_weights")

    main(args)
