package com.leroymerlin.commit;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Enumeration;

/**
 * @author Damien Arrachequesne
 */
public class CommitPanel implements ItemListener {
    private JPanel mainPanel;
    private JComboBox<String> changeScope;
    private JTextField shortDescription;
    private JTextArea longDescription;
    private JLabel closedIssuesLabel;
    private JTextField closedIssues;
    private JRadioButton featRadioButton;
    private JRadioButton fixRadioButton;
    private JRadioButton docsRadioButton;
    private JRadioButton styleRadioButton;
    private JRadioButton refactorRadioButton;
    private JRadioButton perfRadioButton;
    private JRadioButton buildRadioButton;
    private JRadioButton revertRadioButton;
    private ButtonGroup changeTypeGroup;

    CommitPanel(Project project, CommitMessage commitMessage) {
        File workingDirectory = new File(project.getBasePath());
        GitLogQuery.Result result = new GitLogQuery(workingDirectory).execute();
        if (result.isSuccess()) {
            changeScope.addItem(""); // no value by default
            result.getScopes().forEach(changeScope::addItem);
        }

        if (commitMessage != null) {
            restoreValuesFromParsedCommitMessage(commitMessage);
        }

        featRadioButton.addItemListener(this);
        fixRadioButton.addItemListener(this);
        docsRadioButton.addItemListener(this);
        styleRadioButton.addItemListener(this);
        refactorRadioButton.addItemListener(this);
        perfRadioButton.addItemListener(this);
        buildRadioButton.addItemListener(this);
        revertRadioButton.addItemListener(this);

        showFixView(fixRadioButton.isSelected());
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    CommitMessage getCommitMessage() {
        return new CommitMessage(
                getSelectedChangeType(),
                (String) changeScope.getSelectedItem(),
                shortDescription.getText().trim(),
                longDescription.getText().trim(),
                closedIssues.getText().trim());
    }

    private ChangeType getSelectedChangeType() {
        for (Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return ChangeType.valueOf(button.getActionCommand().toUpperCase());
            }
        }
        return null;
    }

    private void restoreValuesFromParsedCommitMessage(CommitMessage commitMessage) {
        if (commitMessage.getChangeType() != null) {
            for (Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements(); ) {
                AbstractButton button = buttons.nextElement();

                if (button.getActionCommand().equalsIgnoreCase(commitMessage.getChangeType().label())) {
                    button.setSelected(true);
                }
            }
        }
        changeScope.setSelectedItem(commitMessage.getChangeScope());
        shortDescription.setText(commitMessage.getShortDescription());
        longDescription.setText(commitMessage.getLongDescription());
        closedIssues.setText(commitMessage.getClosedIssues());
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        showFixView(itemEvent.getSource() == fixRadioButton);
    }

    private void showFixView(boolean show) {
        closedIssuesLabel.setVisible(show);
        closedIssues.setVisible(show);
    }
}
