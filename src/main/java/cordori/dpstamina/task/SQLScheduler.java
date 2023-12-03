package cordori.dpstamina.task;

import cordori.dpstamina.dataManager.SQLManager;

public class SQLScheduler implements Runnable {
    @Override
    public void run() {
        SQLManager.sql.saveAllData();
    }
}
