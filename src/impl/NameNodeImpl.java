package impl;
//TODO: your implementation
import api.NameNodePOA;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import utils.FileDesc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;


public class NameNodeImpl extends NameNodePOA {
    ArrayList<FileDesc> fileInfos = initFileDescs();


    // 初始化时把硬盘上的Fsimage读取到内存
    public ArrayList<FileDesc> initFileDescs(){
        try {
            ArrayList<FileDesc> fileInfos = new ArrayList<>();
            File file = new File("FSImage.json");
            FileReader fr = new FileReader(file);
            StringBuilder str = new StringBuilder();
            int ch = 0;
            while((ch = fr.read()) != -1){
                str.append((char) ch);
            }

            Gson gson=new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonElements = jsonParser.parse(String.valueOf(str)).getAsJsonArray();//获取JsonArray对象
            for (JsonElement fileInfo : jsonElements){
                FileDesc fileInfo1 = gson.fromJson(fileInfo,FileDesc.class);
                fileInfos.add(fileInfo1);
            }
            System.out.println("namenode:init, current file num:"+fileInfos.size());
            return fileInfos;
        }catch (IOException e){
            System.out.println("io exception");
            return new ArrayList<>();
        }

    }


//    NameNode作为服务端，维护文件系统的元数据信息，包括文件目录结构、 文
//    件大小、 文件所在的DataNode以及文件划分的数据块信息等。 它会响应客户端
//    的请求，并返回相关的元数据信息。 它的主要功能如下：
//    - 管理文件系统的命名空间，包括文件和目录的创建、 删除、 移动、 重命
//    名等操作；
//    - 维护数据块的映射关系，即每个数据块所在的DataNode列表；
//    - 处理客户端的文件读写请求，并返回相应的DataNode地址和数据块位置
//    信息；

//    - 维护FsImage，文件系统的命名空间和目录结构，管理文件的元数据信
//    息，包括文件大小、 所在的DataNode以及文件划分的数据块信息等。
//    - 响应客户端的请求，返回相应的元数据信息。

    /**
     *
     * @param mode (0b01-R, 0b10-W, 0b11-R/W)
     * if file does not exist, create.
     * @return FileDesc for the specified file if Mode is permitted, else null
     * e.g., if the file is opened with W mode, but other client has the same file opened previously with mode W or WR,
     * then this call should return null.
     */

//    Namenode在内存中保存着整个文件系统的名字空间和文件数据块映射(Blockmap)的映像。
//    open(filepath, r/w4)：返回文件的元数据，包括大小、 文件
//    块 所 在DataNode（ 一 个 文 件 由 多 个 块 组 成 ， 可 能 分 布 在 不 同
//    的DataNode上）、 创建时间、 修改时间和访问时间5。 若路径指
//    向不存在的文件，则新建文件。 当前文件正在被写入时，新
//    的open(filename,w)请求将会返回null
    @Override
    public String open(String filepath, int mode) {
        System.out.println("fileinfo size:"+fileInfos.size());
        for (FileDesc fileInfo : fileInfos) {
            if (Objects.equals(fileInfo.getName(), filepath)){
                System.out.println("open file:"+fileInfo.toString());
                fileInfo.setMode(mode);
                if ((mode==0b10 || mode == 0b11) && fileInfo.getStatus()==1){
                    System.out.println("namenode: open: opened by other writer");
                    return "";
                }
                if (mode==0b10 || mode == 0b11){
                    fileInfo.setStatus(1);
                }
                System.out.println("namenode: open: "+fileInfo.toString());
                return fileInfo.toString();
            }
        }
        System.out.println("namenode: create file");
        FileDesc newFileInfo = new FileDesc(filepath);
        newFileInfo.setMode(mode);
        newFileInfo.setAccessTime(System.currentTimeMillis());
//        ArrayList<Integer> locations = new ArrayList<>();
//        locations.add((int)(Math.random()*(2)));
//        newFileInfo.setLocations(locations);
        if (mode==0b10 || mode == 0b11){
            newFileInfo.setStatus(1);
        }
        fileInfos.add(newFileInfo);
        return newFileInfo.toString();
    }

    FileDesc getFileDescById(long id){
        for (FileDesc fileDesc :
                fileInfos) {
            if (id == fileDesc.getId()){
                return fileDesc;
            }
        }
        return null;
    }

    public String getLocations(long id,int size){
        FileDesc fileDesc = getFileDescById(id);
        if (fileDesc.getLocations().isEmpty()){
            ArrayList<Integer> locations = new ArrayList<>();
            for (int i=0;i<size;i++){
                locations.add((int)(Math.random()*(2)));
            }
            System.out.println("namenode:create location:"+locations.toString());
            return locations.toString();
        } else
            return fileDesc.getLocations().toString();
    }

    public void updateFileDesc(String fileDescStr,long id){
//        FileDesc fileDesc = getFileDescById(id);
        for (int i = 0; i < fileInfos.size(); i++) {
            if (fileInfos.get(i).getId()==id){
                fileInfos.set(i, FileDesc.fromString(fileDescStr));
            }
        }
    }

    public static String listToJson(ArrayList<FileDesc> list) {

        Gson gson = new Gson();
        String json = gson.toJson(list);
        System.out.println("json"+json);
        return json;
    }

//    close(filepath)：更新文件的元数据写入硬盘
    @Override
    public void close(String fileInfo) {
        FileDesc fileDesc = FileDesc.fromString(fileInfo);
        updateFileDesc(fileInfo,fileDesc.getId());
        FileDesc fileDesc1 = getFileDescById(fileDesc.getId());
        fileDesc1.setStatus(0);
        fileDesc1.setModifyTime(System.currentTimeMillis());
        System.out.println("namenode:close:"+fileDesc1.toString());
        try {
            FileOutputStream f = new FileOutputStream("FSImage.json");
            f.write(listToJson(fileInfos).getBytes(StandardCharsets.UTF_8));
            f.flush();
            f.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
