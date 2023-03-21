package impl;
//TODO: your implementation
import api.NameNodePOA;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import utils.FileDesc;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;


public class NameNodeImpl extends NameNodePOA {
    ArrayList<FileDesc> fileInfos = initFileDescs();


    // ��ʼ��ʱ��Ӳ���ϵ�Fsimage��ȡ���ڴ�
    public ArrayList<FileDesc> initFileDescs(){

        try {
            ArrayList<FileDesc> fileInfos = new ArrayList<>();
            File file = new File("src/server/fs/fsimage.json");
            // ���� FileReader ����
            FileReader fr = new FileReader(file);
            StringBuilder str = new StringBuilder();
            int ch = 0;
            while((ch = fr.read()) != -1){
                str.append((char) ch);
            }

            Gson gson=new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonElements = jsonParser.parse(String.valueOf(str)).getAsJsonArray();//��ȡJsonArray����
            for (JsonElement fileInfo : jsonElements){
                FileDesc fileInfo1 = gson.fromJson(fileInfo,FileDesc.class);
                fileInfos.add(fileInfo1);
            }
            return fileInfos;
        }catch (IOException e){
            System.out.println("io exception");
            return new ArrayList<>();
        }

    }

//    NameNode��Ϊ����ˣ�ά���ļ�ϵͳ��Ԫ������Ϣ�������ļ�Ŀ¼�ṹ�� ��
//    ����С�� �ļ����ڵ�DataNode�Լ��ļ����ֵ����ݿ���Ϣ�ȡ� ������Ӧ�ͻ���
//    �����󣬲�������ص�Ԫ������Ϣ�� ������Ҫ�������£�
//    - �����ļ�ϵͳ�������ռ䣬�����ļ���Ŀ¼�Ĵ����� ɾ���� �ƶ��� ����
//    ���Ȳ�����
//    - ά�����ݿ��ӳ���ϵ����ÿ�����ݿ����ڵ�DataNode�б�
//    - ����ͻ��˵��ļ���д���󣬲�������Ӧ��DataNode��ַ�����ݿ�λ��
//    ��Ϣ��

//    - ά��FsImage���ļ�ϵͳ�������ռ��Ŀ¼�ṹ�������ļ���Ԫ������
//    Ϣ�������ļ���С�� ���ڵ�DataNode�Լ��ļ����ֵ����ݿ���Ϣ�ȡ�
//    - ��Ӧ�ͻ��˵����󣬷�����Ӧ��Ԫ������Ϣ��

    /**
     *
     * @param mode (0b01-R, 0b10-W, 0b11-R/W)
     * if file does not exist, create.
     * @return FileDesc for the specified file if Mode is permitted, else null
     * e.g., if the file is opened with W mode, but other client has the same file opened previously with mode W or WR,
     * then this call should return null.
     */

//    Namenode���ڴ��б����������ļ�ϵͳ�����ֿռ���ļ����ݿ�ӳ��(Blockmap)��ӳ��
//    open(filepath, r/w4)�������ļ���Ԫ���ݣ�������С�� �ļ�
//    �� �� ��DataNode�� һ �� �� �� �� �� �� �� �� �� �� �� �� �� �� �� �� ͬ
//    ��DataNode�ϣ��� ����ʱ�䡢 �޸�ʱ��ͷ���ʱ��5�� ��·��ָ
//    �򲻴��ڵ��ļ������½��ļ��� ��ǰ�ļ����ڱ�д��ʱ����
//    ��open(filename,w)���󽫻᷵��null
    @Override
    public String open(String filepath, int mode) {
        System.out.println("open");
        for (FileDesc fileInfo : fileInfos) {
            if (Objects.equals(fileInfo.getName(), filepath)){
                System.out.println("equal"+fileInfo.toString());
                fileInfo.setMode(mode);
                if ((mode==0b10 || mode == 0b11) && fileInfo.getStatus()==1){
                    return null;
                }
                if (mode==0b10 || mode == 0b11){
                    fileInfo.setStatus(1);
                }
                return fileInfo.toString();
            }
        }
        FileDesc newFileInfo = new FileDesc(filepath);
        newFileInfo.setMode(mode);
        ArrayList<Integer> locations = new ArrayList<>();
        locations.add(0);
        newFileInfo.setLocations(locations);
        if (mode==0b10 || mode == 0b11){
            newFileInfo.setStatus(1);
        }
        fileInfos.add(newFileInfo);
        return newFileInfo.toString();
    }


//    close(filepath)�������ļ���Ԫ����д��Ӳ��
    @Override
    public void close(String fileInfo) {
        FileDesc fileDesc = FileDesc.fromString(fileInfo);
        try {
            OutputStream f = new FileOutputStream(fileDesc.getName());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
