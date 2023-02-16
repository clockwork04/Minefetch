package org.wamwoowam.PowerMacInfo;

import com.sun.management.OperatingSystemMXBean;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.management.ManagementFactory;

import static org.bukkit.Bukkit.getServer;

public class Loadometer {

    //x86 Debugging
    boolean debug = true;
    private double memUsage;
    private double cpuUsage;
    private double worldSize;
    private boolean worldGrabbed = false;

    public boolean init(Plugin plugin) {

        updateLoad();

        //Scheduled to rerun and reparse every minutes since uptime should.. update.. every once in a while
        //Bukkit scheduler has.. cursed syntax, below is an Initial Delay of 0 Ticks, then 20 Ticks times 60 which means this task runs every minute (60 Seconds)
        var scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this::updateLoad, 0L, 20L * 60L);

        // We need the world to load before querying its folder :'3
        scheduler.runTaskTimerAsynchronously(plugin, this::updateWorldSize, 30L, 20L * 60L);

        //grabs TPS ;3
        scheduler.runTaskTimerAsynchronously(plugin, new TPS(), 100L, 1L);
        //i mean i guess it makes sense.. this is a minecraft plugin and minecraft uses ticks.. funny this is required though to get real time
        return true;
    }

    // Update an "commits" load
    public void updateLoad() {
        // Set CPU Usage
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        this.cpuUsage = osBean.getSystemCpuLoad() * 100;


        // Set Memory Usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        this.memUsage = ((double)usedMemory / (double)totalMemory) * 100.0;
    }

    public void updateWorldSize() {
        // Grab World Size
        File worldDirectory = new File(getServer().getWorldContainer(), getServer().getWorlds().get(0).getName());
        long sizeInBytes = getDirectorySize(worldDirectory);
        double sizeInGB = sizeInBytes / (1024.0 * 1024.0);
        this.worldSize = sizeInGB;
        worldGrabbed = true;
    }

    public double getCPULoad() {;
        return this.cpuUsage;
    }

    public double getMEMLoad() {;
        return this.memUsage;
    }

    public double getWorldSize() {
        if(!worldGrabbed){
            return 0.0;
        }
        return worldSize;
    }

    private long getDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getDirectorySize(file);
                }
            }
        }
        return size;
    }

    public String barBuilder(double percentageFree, boolean usedLabel){

        ChatColor wrapColor = ChatColor.WHITE;

        StringBuilder sb = new StringBuilder();

        ChatColor color = ChatColor.GOLD;
        if (percentageFree >= 60){
            color = ChatColor.RED;
        }
        else if (percentageFree >= 35){
            color = ChatColor.YELLOW;
        }
        else {
            color = ChatColor.GREEN;
        }

        String bar = "";
        int looped = 0;

        while (looped++ < (percentageFree/5) ){
            bar += '#';
        }
        //bar = String.format("%-20s",bar);
        bar += ChatColor.WHITE;
        while (looped++ <= 20){
            bar += '_';
        }
        if(usedLabel) {
            return (wrapColor+"["+color+bar+wrapColor+"] " + " ("+ Math.round(percentageFree)+"%) USED");
        }else {
            return (wrapColor+"["+color+bar+wrapColor+"] " + " ("+ Math.round(percentageFree)+"%)");
        }

    }
}
