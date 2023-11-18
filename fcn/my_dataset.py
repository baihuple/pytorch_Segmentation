import os

import torch.utils.data as data
from PIL import Image

''' 自定义数据集 '''
class VOCSegmentation(data.Dataset):
    def __init__(self, voc_root, year="2012", transforms=None, txt_name: str = "train.txt"):
        super(VOCSegmentation, self).__init__()
        # 检查年份
        assert year in ["2007", "2012"], "year must be in ['2007', '2012']"
        # 路径拼接
        root = os.path.join(voc_root, "VOCdevkit", f"VOC{year}")
        assert os.path.exists(root), "path '{}' does not exist.".format(root)
        image_dir = os.path.join(root, 'JPEGImages')                        # 图片路径
        mask_dir = os.path.join(root, 'SegmentationClass')                  # 对应标签路径

        txt_path = os.path.join(root, "ImageSets", "Segmentation", txt_name)
        assert os.path.exists(txt_path), "file '{}' does not exist.".format(txt_path)
        with open(os.path.join(txt_path), "r") as f:
            file_names = [x.strip() for x in f.readlines() if len(x.strip()) > 0]       # 读取文件，若不为空行读取信息，并且去掉前后空格

        self.images = [os.path.join(image_dir, x + ".jpg") for x in file_names]
        self.masks = [os.path.join(mask_dir, x + ".png") for x in file_names]
        assert (len(self.images) == len(self.masks))
        self.transforms = transforms

    def __getitem__(self, index):
        """
        Args:
            index (int): Index

        Returns:
            tuple: (image, target) where target is the image segmentation.
        """
        img = Image.open(self.images[index]).convert('RGB')     # 打开图片并转化为 3 通道
        target = Image.open(self.masks[index])                  # 打开默认调色板模式，为单通道，每个 pixel 的值即为 “类别真实值”

        if self.transforms is not None:
            img, target = self.transforms(img, target)

        return img, target

    def __len__(self):
        return len(self.images)

    # 自定义实现数据的打包过程
    @staticmethod
    def collate_fn(batch):      # [(image_tensor1,target1),(image_tensor2,target2),(image_tensor3,target3)] --- list(tuple)
        images, targets = list(zip(*batch))
        batched_imgs = cat_list(images, fill_value=0)
        batched_targets = cat_list(targets, fill_value=255)
        return batched_imgs, batched_targets

''' 进行打包 '''
def cat_list(images, fill_value=0):
    # 计算该batch数据中，channel, h, w 每一维度的最大值( rgb-image-channel=3, h\w 求最大值)
    # val 验证集的数据我们并没有对大小进行要求
    # 所以需要用最大值 Tensor 打包，确保每个都放得下 - max_szie(c,w,h)
    max_size = tuple(max(s) for s in zip(*[img.shape for img in images]))
    
    # len(images) - 获取图片个数, 加上 batch 维度 - batch_shape(batch_size,c,w,h)
    batch_shape = (len(images),) + max_size
    # 将第一张图片 tensor 重构成 batch_shape 的样子,且内容全部 fill 为0
    batched_imgs = images[0].new(*batch_shape).fill_(fill_value)
    for img, pad_img in zip(images, batched_imgs):
        pad_img[..., :img.shape[-2], :img.shape[-1]].copy_(img)
    return batched_imgs


# dataset = VOCSegmentation(voc_root="/data/", transforms=get_transform(train=True))
# d1 = dataset[0]
# print(d1)
