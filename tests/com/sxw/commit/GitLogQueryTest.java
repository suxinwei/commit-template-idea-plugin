package com.sxw.commit;

import com.sxw.commit.util.GitLogQuery;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class GitLogQueryTest {

    @Test
    @Ignore("manual testing")
    public void testExecute() {
        GitLogQuery.Result result = new GitLogQuery(new File("./"), "git log -10 --format=%s").execute();
        System.out.println(result.isSuccess());
        System.out.println(result.getLogs());
    }

}