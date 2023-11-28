from collections import OrderedDict

from typing import Dict

import torch
from torch import nn, Tensor
from torch.nn import functional as F
from .backbone import resnet50, resnet101


class IntermediateLayerGetter(nn.ModuleDict):
    """
    Module wrapper that returns intermediate layers from a model

    It has a strong assumption that the modules have been registered
    into the model in the same order as they are used.
    This means that one should **not** reuse the same nn.Module
    twice in the forward if you want this to work.

    Additionally, it is only able to query submodules that are directly
    assigned to the model. So if `model` is passed, `model.feature1` can
    be returned, but not `model.feature1.layer2`.

    Args:
        model (nn.Module): model on which we will extract the features
        return_layers (Dict[name, new_name]): a dict containing the names
            of the modules for which the activations will be returned as
            the key of the dict, and the value of the dict is the name
            of the returned activation (which the user can specify).
    """
    _version = 2
    __annotations__ = {
        "return_layers": Dict[str, str],
    }

    def __init__(self, model: nn.Module, return_layers: Dict[str, str]) -> None:
        # 判断构建的网络中是否含有 layer3 / layer4
        if not set(return_layers).issubset([name for name, _ in model.named_children()]):
            raise ValueError("return_layers are not present in model")
        
        # 记录原来的 return_layers，创建字符串形式
        orig_return_layers = return_layers
        return_layers = {str(k): str(v) for k, v in return_layers.items()}

        # 重新构建backbone
        # <1> 将没有使用到的模块全部删掉[ layer4 之后的 avgpool、fc 删除 ]
        layers = OrderedDict()                              # 创建有序字典
        for name, module in model.named_children():         # 遍历 backbone 所有子模块
            layers[name] = module                           # 名称、数据存在有序字典中
            if name in return_layers:                       # 若再 return_layers 中，删除
                del return_layers[name]
            if not return_layers:
                break

        # <2> 传入父类，进行重构组网
        super(IntermediateLayerGetter, self).__init__(layers)
        self.return_layers = orig_return_layers

    # 正向传播的过程
    def forward(self, x: Tensor) -> Dict[str, Tensor]:
        out = OrderedDict()
        for name, module in self.items():      # 遍历子模块，x 为预测数据
            x = module(x)                      # 对每个子模块进行正向传播 
            if name in self.return_layers:     # 判断为哪种输出：'aux' or 'out'  
                out_name = self.return_layers[name]
                out[out_name] = x
        return out


class FCN(nn.Module):
    """
    Implements a Fully-Convolutional Network for semantic segmentation.

    Args:
        backbone (nn.Module): the network used to compute the features for the model.
            The backbone should return an OrderedDict[Tensor], with the key being
            "out" for the last feature map used, and "aux" if an auxiliary classifier
            is used.
        classifier (nn.Module): module that takes the "out" element returned from
            the backbone and returns a dense prediction.
        aux_classifier (nn.Module, optional): auxiliary classifier used during training
    """
    __constants__ = ['aux_classifier']

    def __init__(self, backbone, classifier, aux_classifier=None):
        super(FCN, self).__init__()
        self.backbone = backbone
        self.classifier = classifier
        self.aux_classifier = aux_classifier

    def forward(self, x: Tensor) -> Dict[str, Tensor]:
        # shape - 初始< batch,channel,h,w >
        input_shape = x.shape[-2:]      # 记录最后两个值

        # 1.经过 backbone
        # feature 是有序字典 
        # eg: if include aux: ['out':result1,'aux':result2] or if not include aux: ['out':result]
        features = self.backbone(x)

        # 2.处理 backbone 的输出（主分支部分）
        result = OrderedDict()
        x = features["out"]         # layer4 的输出,输入到主分类器 FCN_Head 中
        x = self.classifier(x)

        # 原论文中虽然使用的是ConvTranspose2d，但权重是冻结的，所以就是一个bilinear插值
        # 采用双线性插值算法还原回 input_shape < 上采样 >
        x = F.interpolate(x, size=input_shape, mode='bilinear', align_corners=False)
        # 存储主分支输出结果
        result["out"] = x

        # 3.处理 backbone 的输出（aux 辅助分类器部分）
        if self.aux_classifier is not None:
            x = features["aux"]
            x = self.aux_classifier(x)
            # 原论文中虽然使用的是ConvTranspose2d，但权重是冻结的，所以就是一个bilinear插值
            # 双线性插值，还原回原图大小
            x = F.interpolate(x, size=input_shape, mode='bilinear', align_corners=False)
            result["aux"] = x

        return result

''' 构建辅助分类器 '''
class FCNHead(nn.Sequential):
    def __init__(self, in_channels, channels):     
        inter_channels = in_channels // 4       # FCN Head 中通过第一个卷积层后 deep:1024 -> 1024/4 = 256
        layers = [
            nn.Conv2d(in_channels, inter_channels, 3, padding=1, bias=False),
            nn.BatchNorm2d(inter_channels),
            nn.ReLU(),
            nn.Dropout(0.1),
            nn.Conv2d(inter_channels, channels, 1)
        ]

        super(FCNHead, self).__init__(*layers)


def fcn_resnet50(aux, num_classes=21, pretrain_backbone=False):
    # 'resnet50_imagenet': 'https://download.pytorch.org/models/resnet50-0676ba61.pth'
    # 'fcn_resnet50_coco': 'https://download.pytorch.org/models/fcn_resnet50_coco-1167a1af.pth'
    backbone = resnet50(replace_stride_with_dilation=[False, True, True])   # 在 make_layer 的 2、3、4 层使用系数，膨胀 or 不膨胀,layer3\4 进行膨胀

    # 载入resnet50 backbone预训练权重
    if pretrain_backbone:
        backbone.load_state_dict(torch.load("resnet50.pth", map_location='cpu'))

    out_layer = 'layer4'
    out_inplanes = 2048     # layer 4 - 输出 channel
    aux_layer = 'layer3'    # 辅助分类器
    aux_inplanes = 1024     # layer 3 - 输出 channel 

    #return_layers = {'layer4': 'out'}
    return_layers = {out_layer:'out'}

    if aux:
        # return_layers['layer3'] = 'aux'
        return_layers[aux_layer] = 'aux'

    # IntermediateLayerGetter - 重构 backbone
    backbone = IntermediateLayerGetter(backbone, return_layers=return_layers)


    # 构建辅助分类器的 FCNHead - classifier
    aux_classifier = None
    # why using aux: https://github.com/pytorch/vision/issues/4292
    if aux:
        aux_classifier = FCNHead(aux_inplanes, num_classes) 


    # 构建主分支上的 FCNHead - classifier   
    classifier = FCNHead(out_inplanes, num_classes)


    # 正式构建 ResNet_FCN
    model = FCN(backbone, classifier, aux_classifier)

    return model


def fcn_resnet101(aux, num_classes=21, pretrain_backbone=False):
    # 'resnet101_imagenet': 'https://download.pytorch.org/models/resnet101-63fe2227.pth'
    # 'fcn_resnet101_coco': 'https://download.pytorch.org/models/fcn_resnet101_coco-7ecb50ca.pth'
    backbone = resnet101(replace_stride_with_dilation=[False, True, True])

    if pretrain_backbone:
        # 载入resnet101 backbone预训练权重
        backbone.load_state_dict(torch.load("resnet101.pth", map_location='cpu'))

    out_inplanes = 2048
    aux_inplanes = 1024

    return_layers = {'layer4': 'out'}
    if aux:
        return_layers['layer3'] = 'aux'
    backbone = IntermediateLayerGetter(backbone, return_layers=return_layers)

    aux_classifier = None
    # why using aux: https://github.com/pytorch/vision/issues/4292
    if aux:
        aux_classifier = FCNHead(aux_inplanes, num_classes)

    classifier = FCNHead(out_inplanes, num_classes)

    model = FCN(backbone, classifier, aux_classifier)

    return model
