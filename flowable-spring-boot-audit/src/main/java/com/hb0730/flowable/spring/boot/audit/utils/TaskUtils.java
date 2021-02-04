package com.hb0730.flowable.spring.boot.audit.utils;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

import java.util.List;

/**
 * @author bing_huang
 */
public class TaskUtils extends BaseUtils {

    public static Task getTaskById(String id) {
        return getService().createTaskQuery().taskId(id).singleResult();
    }

    public static List<Task> getTasksByAssignee(String assignee) {
        return getService()
                .createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskAssignee()
                .desc()
                .list();
    }

    public static List<Task> getTaskByProcessInstanceId(String processInstanceId) {
        return getService().createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskAssignee()
                .desc()
                .list();
    }

    public static TaskService getService() {
        return getService(TaskService.class);
    }
}
