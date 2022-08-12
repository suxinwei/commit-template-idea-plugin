package com.sxw.commit.check;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.ui.NonFocusableCheckBox;
import com.sxw.commit.ChangeType;
import com.sxw.commit.CommitMessage;
import com.sxw.commit.util.GitLogQuery;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.List;

import javax.swing.*;


public class CheckCommitTemplateHandler extends CheckinHandler {

    private static final String GIT_REMOTE = "git remote -v";
    private static final String GIT_RFW_COMPONENT_TAG = "engineering/RFWComponent.git";

    private Project myProject;
    private CheckinProjectPanel myCheckinPanel;
    private boolean checkFlag;

    CheckCommitTemplateHandler(Project myProject, CheckinProjectPanel myCheckinPanel) {
        this.myProject = myProject;
        this.myCheckinPanel = myCheckinPanel;
        initCheckFlag();
    }

    private void initCheckFlag() {
        File workingDirectory = new File(myProject.getBasePath());
        GitLogQuery.Result result = new GitLogQuery(workingDirectory, GIT_REMOTE).execute();
        if (!result.isSuccess()) {
            return;
        }
        List<String> logs = result.getLogs();
        for (String log : logs) {
            if (log != null && log.contains(GIT_RFW_COMPONENT_TAG)) {
                checkFlag = true;
                break;
            }
        }
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
                if (checkFlag) {
                    checkBox.setEnabled(false);
                }
                checkBox.setSelected(checkFlag);
                return panel;
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
        }

        String messageStr = myCheckinPanel.getCommitMessage();
        CommitMessage message = CommitMessage.parse(messageStr);

        if (StringUtils.isEmpty(message.getCommitTitle())) {
            showDialog("Commit template invalid, please check \"Commit title\" line style");
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
                if (StringUtils.isEmpty(message.getClosedIssues()) && StringUtils.isEmpty(message.getCommitDesc())) {
                    showDialog("Commit template invalid, please check \"Fixed issues\" line style or \"Commit desc\" line style");
                    return ReturnResult.CANCEL;
                }
            } else {
                if (StringUtils.isEmpty(message.getCommitDesc())) {
                    showDialog("Commit template invalid, please check \"Commit desc\" line style");
                    return ReturnResult.CANCEL;
                }
            }
        }
        return ReturnResult.COMMIT;
    }

    private void showDialog(String message) {
        Messages.showErrorDialog(message, "Check Commit Template");
    }
}
