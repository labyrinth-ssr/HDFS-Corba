package impl;
//TODO: your implementation
import api.DataNodePOA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class DataNodeImpl extends DataNodePOA {

    class Block{
        int id;
        byte[] data = new byte[0];
    }
    ArrayList<Block> blocks = new ArrayList<>(0);
    int size = 0;

//    public DataNodeImpl(){
//        for (int i=0;i<10;i++){
//            Block block = new Block();
//            block.id = i;
//            blocks.add(block);
//        }
//        Block newBlock = new Block();
//        newBlock.data = new byte [0];
//        blocks.add(newBlock);
//    }


    // 每个node
    @Override
    public byte[] read(int block_id) {
        System.out.println("datanode:read");
        Block block= getBlockById(block_id);
        if (block==null)
            return "".getBytes();
        return block.data;
    }

    private Block getBlockById(int id) {
        for (Block block : blocks) {
            if (block.id == id) {
                return block;
            }
        }
        return null;
    }

    @Override
    public void append(int block_id, byte[] bytes) {
        System.out.println("write");
        Block block;
        if ((block = getBlockById(block_id))==null){
            block = new Block();
            block.id = block_id;
            blocks.add(block);
        }
        byte[] byte_3 = new byte[block.data.length+bytes.length];
        System.arraycopy(block.data, 0, byte_3, 0, block.data.length);
        System.arraycopy(bytes, 0, byte_3, block.data.length, bytes.length);
        block.data = byte_3;

        try {
            FileOutputStream fos = new FileOutputStream("block"+block_id+".txt");
            fos.write(byte_3);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int randomBlockId() {
        int min = 0;
        int max = 10;
        int res = min + (int)(Math.random()*(max - min +1));
        System.out.println("res"+res);
        return res;
    }
}
