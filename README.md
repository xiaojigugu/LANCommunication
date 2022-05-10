# 局域网语音通话
```text
通过AudioRecord进行PCM音频数据录制
对PCM数据进行音效处理（噪音抑制、回声消除、自动增强，由于不同手机的处理机制不同，此功能不一定生效）
利用MediaCodec将PCM数据编码为aac数据
通过udp传输数据
利用MediaCodec解码还原PCM数据
通过AudioTrack进行PCM数据播放
```

## DEMO使用流程

1. 准备两台Android设备并分别安装本项目导出apk
2. 启动后授予录音权限
3. 分别填入另一台手机的内网ip（本机内网ip在界面顶部显示）
4. 分别点击 `连接服务` 按钮，此时可见已连接字样
5. 分别点击`开启通话`按钮，此时音频通道已建立并可进行通话

## 模块

> app

```
主模块，demo界面，连接、通话入口
```

> audio

```
音频编解码模块
EncoderConfig.class - 主要音频编解码配置
```

> socket

```
UDP通信模块
```

## 解码

>解码注意事项 - csd_0
```
解码时注意需要设置 `csd_0` 参数,此参数为MediaCodec特有,格式为:
AAC Profile 5bits | 采样率 4bits | 声道数 4bits | 其他 3bits |
```

```text
其中部分参数:
1. AAC Profile
AAC Main 0x01 
AAC LC    0x02 
AAC SSR  0x03

2. 采样率
0x00   96000 
0x01   88200 
0x02   64000 
0x03   48000 
0x04   44100
0x05   32000
0x06   24000 
0x07   22050 
0x08   16000 
0x09   12000 
0x0A   11025 
0x0B    8000 
0x0C   reserved 
0x0D   reserved 
0x0E   reserved 
0x0F   escape value

3. 声道数
0x00 - defined in audioDecderSpecificConfig 
0x01 单声道（center front speaker） 
0x02 双声道（left, right front speakers） 
0x03 三声道（center, left, right front speakers） 
0x04 四声道（center, left, right front speakers, rear surround speakers） 
0x05 五声道（center, left, right front speakers, left surround, right surround rear speakers） 
0x06 5.1声道（center, left, right front speakers, left surround, right surround rear speakers, front low frequency effects speaker) 
0x07 7.1声道（center, left, right center front speakers, left, right outside front speakers, left surround, right surround rear speakers, front low frequency effects speaker) 
0x08-0x0F - reserved
```

```
csd_0计算方式:
按顺序排序-> 音频格式,采样率,声道数,其他(3位，给000即可),
```

```text
eg1:
音频编码格式为：
AAC-LC ,16000，单声道,其他，
则
16制数为:0X02 0X08 0X01 0X00
2进制:0010 1000 0001 000(3位)
2进制转成16进制:001010000001000 -> 1408
得csd_0为: new byte[]{(byte) 0x14, (byte) 0x08};

eg2:
音频编码格式为：
AAC-LC ,44100，双声道,其他，
则
16制数为:0X02 0X04 0X02 0X00
2进制:0010 0100 0010 000(3位)
2进制转成16进制:001001000010000 -> 1210
得csd_0为:data = new byte[]{(byte) 0x12, (byte) 0x10};

以上参考资料见:https://blog.csdn.net/lavender1626/article/details/80431902
```

> 解码注意事项 - adts

```
修改EncoderConfig.class中的采样率需要同时修改采样率Index即 SAMPLE_RATE_FREQUENCY_INDEX，
对用规则如下：
     * 0: 96000 Hz
     * 1: 88200 Hz
     * 2: 64000 Hz
     * 3: 48000 Hz
     * 4: 44100 Hz
     * 5: 32000 Hz
     * 6: 24000 Hz
     * 7: 22050 Hz
     * 8: 16000 Hz
     * 9: 12000 Hz
     * 10: 11025 Hz
     * 11: 8000 Hz
     * 12: 7350 Hz
     * 13: Reserved
     * 14: Reserved
     * 15: frequency is written explictly
```

