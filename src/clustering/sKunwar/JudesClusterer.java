package clustering.sKunwar;

import com.model.DataSet;
import com.model.Topic;

import javax.swing.*;

/**
 * Created by jude8 on 7/22/2016.
 */
public class JudesClusterer {



    public static void main(String args[]){

        String folder;

        JFileChooser folderPicker = new JFileChooser();
        folderPicker.changeToParentDirectory();
        folderPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderPicker.setAcceptAllFileFilterUsed(false);


        DataSet dataset = new DataSet(folderPicker.getSelectedFile().getPath());






    }


    public static void cluster(Topic topic){
        
    }


}
