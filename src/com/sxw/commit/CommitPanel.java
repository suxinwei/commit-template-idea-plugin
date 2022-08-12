package com.sxw.commit;

import com.intellij.openapi.project.Project;
import com.sxw.commit.util.GitLogQuery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.Executors;

import javax.swing.*;

/**
 * @author Damien Arrachequesne
 */
public class CommitPanel implements ItemListener {
    private static final String CHANGE_SCOPE_HINT = "---Select the item below---";
    private static final String GIT_LOG_COMMAND = "git log --all --format=%s";

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
        initChangeScopeData(project);

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

        changeScope.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String changeScopeStr = (String) changeScope.getSelectedItem();
                if (CHANGE_SCOPE_HINT.equals(changeScopeStr)) {
                    changeScope.setSelectedItem("");
                }
            }
        });

        showFixView(fixRadioButton.isSelected());
    }

    private void initChangeScopeData(Project project) {
        changeScope.addItem(CHANGE_SCOPE_HINT);
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                File workingDirectory = new File(project.getBasePath());
                GitLogQuery.Result result = new GitLogQuery(workingDirectory, GIT_LOG_COMMAND).execute();
                if (result.isSuccess()) {
                    CommitMessage.getScopes(result.getLogs()).forEach(changeScope::addItem);
                }
            }
        });
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    CommitMessage getCommitMessage() {
        String closedIssuesResult = null;
        if (fixRadioButton.isSelected()) {
            closedIssuesResult = this.closedIssues.getText().trim();
        }

        return new CommitMessage(
                getSelectedChangeType(),
                (String) changeScope.getSelectedItem(),
                shortDescription.getText().trim(),
                longDescription.getText().trim(),
                closedIssuesResult);
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
        shortDescription.setText(commitMessage.getCommitTitle());
        longDescription.setText(commitMessage.getCommitDesc());
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
