from typing import Dict
import torch
import torch.nn as nn
import torch.nn.functional as F

''' Unet 结构中的成对卷积 < 基本成对出现 > '''
''' in_channel: 经过上一步池化得到的准备输入双卷积结构的 channel '''
''' mid_channels: 经过第一个卷积的输出 channel '''
''' out_channels: 双层结构的输出 channel '''
class DoubleConv(nn.Sequential):
    def __init__(self, in_channels, out_channels, mid_channels=None):
        if mid_channels is None:
            mid_channels = out_channels
        super(DoubleConv, self).__init__(
            # Conv1
            nn.Conv2d(in_channels, mid_channels, kernel_size=3, padding=1, bias=False),
            nn.BatchNorm2d(mid_channels),
            nn.ReLU(inplace=True),
            # Conv2
            nn.Conv2d(mid_channels, out_channels, kernel_size=3, padding=1, bias=False),
            nn.BatchNorm2d(out_channels),
            nn.ReLU(inplace=True)
        )

''' Down: DownSampling<MaxPool>  +  DoubleConv'''
class Down(nn.Sequential):
    def __init__(self, in_channels, out_channels):
        super(Down, self).__init__(
            nn.MaxPool2d(2, stride=2),
            DoubleConv(in_channels, out_channels)
        )

''' Up: UpSampling + Concat<拼接> + DoubleConv '''
''' bilinear: 是否采用 -双线性差值- 替代 - 转置卷积 '''
''' in_channels: 经过双线性差值后、拼接之后的 channels or Up 模块第一个卷积层输入的 channels'''
class Up(nn.Module):
    def __init__(self, in_channels, out_channels, bilinear=True):
        super(Up, self).__init__()
        if bilinear:    # <作者实现>
            # 上采样层
            self.up = nn.Upsample(scale_factor=2, mode='bilinear', align_corners=True)      # scale_factor 为上采样率
            # 双卷积层
            self.conv = DoubleConv(in_channels, out_channels, in_channels // 2)
        else:
            # <原论文中的实现：注意 UpSampling 的构造 && DoubleConv 的输出有点不同>
            self.up = nn.ConvTranspose2d(in_channels, in_channels // 2, kernel_size=2, stride=2)
            self.conv = DoubleConv(in_channels, out_channels)

    # x1: 需要上采样的特征层；x2：需要x1上采样后进行拼接的特征层
    def forward(self, x1: torch.Tensor, x2: torch.Tensor) -> torch.Tensor:
        x1 = self.up(x1)                                    # x1 上采样

        # 在拼接前进行 x1 UpSampling 后的 padding：防止 x1,x2 拼接尺寸不对
        # 尺寸出错原因：原 input 需要进行四次下采样到最后，故 H,W 缩小未原来的 1/16
        # 若原 H,W 输入不是 16 的整数倍，可能导致尺寸出现差错
        # [N, C, H, W]
        diff_y = x2.size()[2] - x1.size()[2]        # get x2.height - x1.height
        diff_x = x2.size()[3] - x1.size()[3]        # get x2.width - x1.width
        # padding_left, padding_right, padding_top, padding_bottom
        x1 = F.pad(x1, [diff_x // 2, diff_x - diff_x // 2,
                        diff_y // 2, diff_y - diff_y // 2])

        x = torch.cat([x2, x1], dim=1)                      # x2,x1 在 dim=1 即 channel 维度进行拼接
        x = self.conv(x)
        return x

''' 最后输出前的 1*1 卷积层: without BN && Relu '''
class OutConv(nn.Sequential):
    def __init__(self, in_channels, num_classes):
        super(OutConv, self).__init__(
            nn.Conv2d(in_channels, num_classes, kernel_size=1)
        )


class UNet(nn.Module):
    def __init__(self,
                 in_channels: int = 1,
                 num_classes: int = 2,
                 bilinear: bool = True,         # Bilinear replace TransConv ?
                 base_c: int = 64):             # base_c: Unet 网络中图像刚输入后经过的第一个卷积层的 kernal 个数
        super(UNet, self).__init__()
        self.in_channels = in_channels
        self.num_classes = num_classes
        self.bilinear = bilinear

        # 最开始的两个卷积层
        self.in_conv = DoubleConv(in_channels, base_c)   

        # 四个下采样
        self.down1 = Down(base_c, base_c * 2)
        self.down2 = Down(base_c * 2, base_c * 4)
        self.down3 = Down(base_c * 4, base_c * 8)
        factor = 2 if bilinear else 1           # down4's output channel is up to the way of UpSampling
        self.down4 = Down(base_c * 8, base_c * 16 // factor)

        # 四个上采样: output channel is up to the way of UpSampling too
        self.up1 = Up(base_c * 16, base_c * 8 // factor, bilinear)
        self.up2 = Up(base_c * 8, base_c * 4 // factor, bilinear)
        self.up3 = Up(base_c * 4, base_c * 2 // factor, bilinear)
        self.up4 = Up(base_c * 2, base_c, bilinear)
        self.out_conv = OutConv(base_c, num_classes)

    def forward(self, x: torch.Tensor) -> Dict[str, torch.Tensor]:
        x1 = self.in_conv(x)
        x2 = self.down1(x1)
        x3 = self.down2(x2)
        x4 = self.down3(x3)
        x5 = self.down4(x4)
        x = self.up1(x5, x4)
        x = self.up2(x, x3)
        x = self.up3(x, x2)
        x = self.up4(x, x1)
        logits = self.out_conv(x)

        return {"out": logits}
