import os
import time
import json
import torch
from torchvision import transforms
import numpy as np
from PIL import Image
from src import fcn_resnet50


def time_synchronized():
    torch.cuda.synchronize() if torch.cuda.is_available() else None
    return time.time()


def main():
    aux = False  # inference time not need aux_classifier
    classes = 20
    weights_path = "./save_weights/model_4.pth"        # 改成自己的名称(已自创) -- 导入自己训练网络的相关权重
    img_path = "./test.jpg"                             # 预测图片<自下载>
    palette_path = "./palette.json"                     # 调色板<类别划分>

    # 检查相关路径是否存在
    assert os.path.exists(weights_path), f"weights {weights_path} not found."
    assert os.path.exists(img_path), f"image {img_path} not found."
    assert os.path.exists(palette_path), f"palette {palette_path} not found."
    with open(palette_path, "rb") as f:
        pallette_dict = json.load(f)
        pallette = []
        for v in pallette_dict.values():
            pallette += v

    # get devices 获取设备
    device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
    print("using {} device.".format(device))

    # create model 创建模型
    model = fcn_resnet50(aux=aux, num_classes=classes+1)

    # delete weights about aux_classifier  - 导入刚刚训练好的模型权重
    weights_dict = torch.load(weights_path, map_location='cpu')['model']
    for k in list(weights_dict.keys()):
        if "aux" in k:
            del weights_dict[k]

    # load weights  载入权重到设备
    model.load_state_dict(weights_dict)
    model.to(device)

    # load image 读取图片
    original_img = Image.open(img_path)
 
    # from pil image to tensor and normalize  预处理
    data_transform = transforms.Compose([transforms.Resize(520),
                                         transforms.ToTensor(),
                                         transforms.Normalize(mean=(0.485, 0.456, 0.406),
                                                              std=(0.229, 0.224, 0.225))])
    img = data_transform(original_img)

    # expand batch dimension
    # 在 dim = 0 增加一个 batch 维度 (channel,w,h) ---> (batch_size,channel,w,h)
    img = torch.unsqueeze(img, dim=0)

    model.eval()  # 进入验证模式
    with torch.no_grad(): 
        # init model    传入像素值全为 0 的图片，初始化模型
        img_height, img_width = img.shape[-2:]      # <获取图片的后两维 H,W >
        init_img = torch.zeros((1, 3, img_height, img_width), device=device)
        model(init_img)

        t_start = time_synchronized()
        output = model(img.to(device))      # 正式开始预测
        t_end = time_synchronized()
        print("inference time: {}".format(t_end - t_start))

        prediction = output['out'].argmax(1).squeeze(0)     # 提取主输出 .argmax(1) 指认类别 .squeeze(0) 压缩掉 batch_size 维度
        prediction = prediction.to("cpu").numpy().astype(np.uint8)
        mask = Image.fromarray(prediction)          # 读取图片
        mask.putpalette(pallette)                   # 设置调色板
        mask.save("test_result.png")                # 保存图片


if __name__ == '__main__':
    main()
