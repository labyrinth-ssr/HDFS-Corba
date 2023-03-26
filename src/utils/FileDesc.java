package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Field;
import java.util.ArrayList;


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
    int fd;
    ArrayList<Integer> locations = new ArrayList<>();
    int[] children;
    String size;
    int status; // 1 opened,0 cloded
    boolean excluded = false;

    public int getFd() {
        return fd;
    }

    public void setFd(int fd) {
        this.fd = fd;
    }

    public static void setId(long id) {
        FileDesc.id = id;
    }



    public FileDesc(String name) {
        this.id = id++;
        this.name = name;
    }
    int mode;


    public ArrayList<Integer> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Integer> locations) {
        this.locations = locations;
    }

    public long getId() {
        return id;
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

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
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
        return  fileInfo;
    }

    public static FileDesc fromString(String str){
        if (str == "")
            return null;
        return jsonToJavaBean(str);
    }
}
