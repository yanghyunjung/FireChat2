package net.skhu.firechat2.Room.Thread;

import android.util.Log;

import net.skhu.firechat2.Item.Item;

import java.io.File;

public class RemoveFileThread implements Runnable {
    Item item;
    File file;

    public RemoveFileThread(Item item, File file) {
        this.item = item;
        this.file = file;
    }

    public void run() {
        removeFile(item, file);
    }

    private void removeFile(Item item, File file){
        if(item.getHavePhoto()) {
            //File file = context.getFilesDir();

            removeFile(new File(file, item.getPhotoFileName()));
        }
        else if(item.getHaveVideo()) {
            //File file = context.getFilesDir();

            removeFile(new File(file, item.getVideoFileName()));
        }
        else if(item.getHaveMusic()){
            //File file = context.getFilesDir();

            removeFile(new File(file, item.getMusicFileName()));
        }
    }

    private void removeFile(File removeFile){
        if(removeFile.exists()) {
            if (removeFile.delete()) {
                Log.i("pjw", "file remove" + removeFile.getName() + "삭제성공");
            } else {
                Log.i("pjw", "file remove" + removeFile.getName() + "삭제실패");
            }
        }
    }
}