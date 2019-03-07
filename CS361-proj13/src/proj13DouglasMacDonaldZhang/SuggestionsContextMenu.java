package proj13DouglasMacDonaldZhang;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.CodeArea;
import proj13DouglasMacDonaldZhang.bantam.semant.IdentifierInfo;

import java.util.ArrayList;

public class SuggestionsContextMenu extends ContextMenu {
    ArrayList<MenuItem> menuItems;

    public SuggestionsContextMenu(ArrayList<IdentifierInfo> suggestionsList, CodeArea codeArea){
        super();
        menuItems = new ArrayList<>();
        suggestionsList.forEach(suggestion ->{
            System.out.println("Making a new menu item");
            MenuItem suggestionItem = new MenuItem();
            suggestionItem.setText(suggestion.getType() + " " + suggestion.getName());
            suggestionItem.setOnAction(event -> replace(codeArea, suggestion.getName()));
            this.getItems().add(suggestionItem);
        });


    }

    private void replace(CodeArea codeArea, String replacer){
        codeArea.replaceSelection(replacer);
    }
}
