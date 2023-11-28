import os
from PIL import Image
import numpy as np
from torch.utils.data import Dataset

''' 自定义数据集 '''
class DriveDataset(Dataset):
    def __init__(self, root: str, train: bool, transforms=None):        # root 指向数据根目录
        super(DriveDataset, self).__init__()
        
        # define params
        self.flag = "training" if train else "test"
        data_root = os.path.join(root, "DRIVE", self.flag)
        assert os.path.exists(data_root), f"path '{data_root}' does not exists."
        self.transforms = transforms
        img_names = [i for i in os.listdir(os.path.join(data_root, "images")) if i.endswith(".tif")]    # 获取图片名称
        self.img_list = [os.path.join(data_root, "images", i) for i in img_names]                       # 获取图片路径
        self.manual = [os.path.join(data_root, "1st_manual", i.split("_")[0] + "_manual1.gif")          # 获取每个 manual 文件的名称
                       for i in img_names]
        # check files if exists or not
        for i in self.manual:
            if os.path.exists(i) is False:
                raise FileNotFoundError(f"file {i} does not exists.")

        # 构建每个 mask 的路径
        self.roi_mask = [os.path.join(data_root, "mask", i.split("_")[0] + f"_{self.flag}_mask.gif")
                         for i in img_names]
        # check files
        for i in self.roi_mask:
            if os.path.exists(i) is False:
                raise FileNotFoundError(f"file {i} does not exists.")

    def __getitem__(self, idx):
        img = Image.open(self.img_list[idx]).convert('RGB')
        manual = Image.open(self.manual[idx]).convert('L')      # convert to gray 灰度图片
        manual = np.array(manual) / 255                         # 转化为前景 1；背景 0 [ 图片为黑白,只有0/255 ]
        roi_mask = Image.open(self.roi_mask[idx]).convert('L')  
        roi_mask = 255 - np.array(roi_mask)
        # 忽略像素值为 255 的元素<不感兴趣区域>
        # 组中设置：不感兴趣区域-255，前景-1，背景-0
        mask = np.clip(manual + roi_mask, a_min=0, a_max=255)

        # 这里转回PIL Image 格式的原因是:transforms中是对PIL数据进行处理
        mask = Image.fromarray(mask)
        # 进行相应的预处理
        if self.transforms is not None:
            img, mask = self.transforms(img, mask)

        return img, mask

    # 返回数据数目
    def __len__(self):
        return len(self.img_list)

    @staticmethod
    def collate_fn(batch):
        images, targets = list(zip(*batch))
        batched_imgs = cat_list(images, fill_value=0)
        batched_targets = cat_list(targets, fill_value=255)
        return batched_imgs, batched_targets


def cat_list(images, fill_value=0):
    max_size = tuple(max(s) for s in zip(*[img.shape for img in images]))
    batch_shape = (len(images),) + max_size
    batched_imgs = images[0].new(*batch_shape).fill_(fill_value)
    for img, pad_img in zip(images, batched_imgs):
        pad_img[..., :img.shape[-2], :img.shape[-1]].copy_(img)
    return batched_imgs

