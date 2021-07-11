package com.sxw.commit;


import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.ui.NonFocusableCheckBox;
import com.leroymerlin.commit.ChangeType;
import com.leroymerlin.commit.CommitMessage;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import javax.swing.*;


public class CheckCommitTemplateHandler extends CheckinHandler {

    private Project myProject;
    private CheckinProjectPanel myCheckinPanel;
    private static boolean checkFlag = true;

    CheckCommitTemplateHandler(Project myProject, CheckinProjectPanel myCheckinPanel) {
        this.myProject = myProject;
        this.myCheckinPanel = myCheckinPanel;
    }

    @Nullable
    @Override
    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        NonFocusableCheckBox checkBox = new NonFocusableCheckBox("Check commit template");
        return new RefreshableOnComponent() {
            @Override
            public JComponent getComponent() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(checkBox);
                boolean dumb = DumbService.isDumb(myProject);
                checkBox.setEnabled(!dumb);
                return panel;
            }

            @Override
            public void refresh() {

            }

            @Override
            public void saveState() {
                checkFlag = checkBox.isSelected();
            }

            @Override
            public void restoreState() {
                checkBox.setSelected(checkFlag);
            }
        };
    }


    @Override
    public ReturnResult beforeCheckin() {
        if (!checkFlag) {
            return ReturnResult.COMMIT;
        } else {
            String messageStr = myCheckinPanel.getCommitMessage();
            CommitMessage message = CommitMessage.parse(messageStr);

            if (StringUtils.isEmpty(message.getShortDescription())) {
                showDialog("Commit template invalid, please check \"Short description\" line style");
                return ReturnResult.CANCEL;
            }

            if (!StringUtils.isEmpty(message.getClosedIssues())) {
                if (message.getChangeType() != ChangeType.FIX) {
                    showDialog("Commit template invalid, please use \"fix\" type");
                    return ReturnResult.CANCEL;
                }
            }

            String[] strings = messageStr.split("\n");
            if (strings.length >= 2) {
                if (message.getChangeType() == ChangeType.FIX) {
                    if (StringUtils.isEmpty(message.getClosedIssues()) && StringUtils.isEmpty(message.getLongDescription())) {
                        showDialog("Commit template invalid, please check \"Fixed issues\" line style or \"Long description\" line style");
                        return ReturnResult.CANCEL;
                    }
                } else {
                    if (StringUtils.isEmpty(message.getLongDescription())) {
                        showDialog("Commit template invalid, please check \"Long description\" line style");
                        return ReturnResult.CANCEL;
                    }
                }
            }
        }
        return ReturnResult.COMMIT;
    }

    private void showDialog(String message) {
        Messages.showErrorDialog(message, "Check Commit Template");
    }
}
