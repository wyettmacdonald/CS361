// https://stackoverflow.com/questions/35070310/javafx-representing-directories
package proj6AbramsDeutschDurstJones;
import java.io.File;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;

public class DirectoryController {

  private TreeView directoryTree;

  public void setDirectoryTree(TreeView tv) {
    this.directoryTree = tv;

    // capture current directory
    File fl = new File(System.getProperty("user.dir"));
    // create the directory tree
    this.directoryTree.setRoot(getNode(fl));
  }

  private TreeItem<String> getNode(File fl) {
    // create root, which is returned at the end
    TreeItem<String> root = new TreeItem<String>(fl.getName());
    for (File f : fl.listFiles()) {
      if (f.isDirectory()) {
	// recursively traverse file directory
	root.getChildren().add(getNode(f));
      } else {
	root.getChildren().add(new TreeItem<String>(f.getName()));
      }
    }
    return root;
  }
}
