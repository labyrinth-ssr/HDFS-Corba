package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Field;


//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
public class FileDesc {
    /* the id should be assigned uniquely during the lifetime of NameNode,
     * so that NameNode can know which client's open has over at close
     * e.g., on nameNode1
     * client1 opened file "Hello.txt" with mode 'w' , and retrieved a FileDesc with 0x889
     * client2 tries opening the same file "Hello.txt" with mode 'w' , and since the 0x889 is not closed yet, the return
     * value of open() is null.
     * after a while client1 call close() with the FileDesc of id 0x889.
     * client2 tries again and get a new FileDesc with a new id 0x88a
     */
    static long id = 0;
    String name;
            int[] locations;
        int[] children;
        String size;
        int status; // 1 opened,0 cloded

    public FileDesc(String name) {
        this.id = id++;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    //    public static void reflect(Object e,StringBuilder sb) throws Exception {
//        Class<? extends Object> cls = e.getClass();
//        Field[] fields = cls.getDeclaredFields();
//        sb.append("{");
//        for (int i = 0; i < fields.length; i++) {
//            Field f = fields[i];
//            f.setAccessible(true);
//            sb.append(f.getName()+":"+f.get(e)+",");
//        }
//        sb.append("}");
//    }

    public static String javabeanToJson(FileDesc fileDesc) {
        Gson gson = new Gson();
        String json = gson.toJson(fileDesc);
        return json;
    }

    /* The following method is for conversion, so we can have interface that return string, which is easy to write in idl */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        try {
//            reflect(this,sb);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        System.out.println(javabeanToJson(this));
//        return sb.toString();
        return javabeanToJson(this);
    }

    /**
     * json to javabean
     *
     * @param json
     */
    public static FileDesc jsonToJavaBean(String json) {
        Gson gson = new Gson();
        FileDesc fileInfo = gson.fromJson(json, FileDesc.class);//对于javabean直接给出class实例
        System.out.println(fileInfo.toString());
        return  fileInfo;
    }

    public static FileDesc fromString(String str){

//        Gson gson=new Gson();
//        JsonParser jsonParser = new JsonParser();
//        JsonArray jsonElements = jsonParser.parse(str).getAsJsonArray();//获取JsonArray对象
//        for (JsonElement fileInfo : jsonElements){
//            FileDesc fileInfo1 = gson.fromJson(fileInfo,FileDesc.class);
//            fileInfos.add(fileInfo1);
//        }

        return jsonToJavaBean(str);
    }
}
