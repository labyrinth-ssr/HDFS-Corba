DataNode 中有多个block，其id被记录
每个数据块有id，
一个文件可能被分成多个数据块
谁负责切分数据块？ client
datanode

如何启动多个服务端？

orbd

设计：如何定位到对应的datanode的blockId。blockId应当是。

# lab1 实验报告

## 设计思路

创建时间、 修改时间和访问时间

### fsimage


## 实现


介绍你的设计思路和实现。 包括但不限于FsImage的
设计，文件和数据块的映射和数据块定位、 多副本的实现、 读数据多
个DateNode可用时的选择策略。