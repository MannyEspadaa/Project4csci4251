import java.util.*;

// Class to represent each job with its attributes
class Job {
    String id; // Job ID
    int arrivalTime; // Arrival time of the job
    int burstTime; // CPU burst time
    int remainingTime; // Time remaining for preemptive scheduling
    int waitingTime = 0; // Waiting time
    int turnaroundTime = 0; // Turnaround time

    Job(String id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}

public class SchedulingSimulator {

    // Method to initialize the list of jobs
    static List<Job> createJobs() {
        return new ArrayList<>(Arrays.asList(
            new Job("A", 0, 16), new Job("B", 3, 2), new Job("C", 5, 1),
            new Job("D", 9, 6), new Job("E", 10, 1), new Job("F", 12, 9),
            new Job("G", 14, 4), new Job("H", 16, 14), new Job("I", 17, 1),
            new Job("J", 19, 8)
        ));
    }

    // Method to print results for each scheduling algorithm
    static void printResults(String name, List<Job> jobs) {
        double totalWT = 0, totalTAT = 0;
        System.out.printf("\n--- %s ---\n", name);
        System.out.printf("%-3s %-6s %-8s\n", "ID", "WT", "TAT");
        for (Job job : jobs) {
            System.out.printf("%-3s %-6d %-8d\n", job.id, job.waitingTime, job.turnaroundTime);
            totalWT += job.waitingTime;
            totalTAT += job.turnaroundTime;
        }
        System.out.printf("Average WT: %.2f ms\n", totalWT / jobs.size());
        System.out.printf("Average TAT: %.2f ms\n", totalTAT / jobs.size());
    }

    // Method to create a deep copy of the job list for reuse across algorithms
    static List<Job> deepCopy(List<Job> original) {
        List<Job> copy = new ArrayList<>();
        for (Job j : original) copy.add(new Job(j.id, j.arrivalTime, j.burstTime));
        return copy;
    }

    // First Come First Serve Scheduling
    static void fcfs(List<Job> jobs) {
        jobs.sort(Comparator.comparingInt(j -> j.arrivalTime)); // Sort jobs by arrival time
        int currentTime = 0;
        for (Job job : jobs) {
            if (currentTime < job.arrivalTime) currentTime = job.arrivalTime;
            job.waitingTime = currentTime - job.arrivalTime;
            job.turnaroundTime = job.waitingTime + job.burstTime;
            currentTime += job.burstTime;
        }
        printResults("FCFS", jobs);
    }

    // Shortest Job Next Scheduling (non-preemptive)
    static void sjn(List<Job> jobs) {
        List<Job> completed = new ArrayList<>();
        PriorityQueue<Job> pq = new PriorityQueue<>(Comparator.comparingInt(j -> j.burstTime));
        int time = 0;
        while (completed.size() < jobs.size()) {
            for (Job job : jobs) {
                if (job.arrivalTime <= time && !completed.contains(job) && !pq.contains(job)) {
                    pq.add(job); // Add job to priority queue if it has arrived and not already in it
                }
            }
            if (!pq.isEmpty()) {
                Job current = pq.poll();
                current.waitingTime = time - current.arrivalTime;
                time += current.burstTime;
                current.turnaroundTime = current.waitingTime + current.burstTime;
                completed.add(current);
            } else {
                time++; // Wait for next job to arrive
            }
        }
        printResults("SJN", completed);
    }

    // Shortest Remaining Time Scheduling (preemptive)
    static void srt(List<Job> jobs) {
        List<Job> completed = new ArrayList<>();
        int time = 0;
        Job current = null;
        while (completed.size() < jobs.size()) {
            Job shortest = null;
            for (Job job : jobs) {
                if (job.arrivalTime <= time && job.remainingTime > 0) {
                    if (shortest == null || job.remainingTime < shortest.remainingTime) {
                        shortest = job; // Find job with shortest remaining time
                    }
                }
            }
            if (shortest != null) {
                shortest.remainingTime--;
                if (shortest.remainingTime == 0) {
                    shortest.turnaroundTime = time + 1 - shortest.arrivalTime;
                    shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;
                    completed.add(shortest);
                }
            }
            time++;
        }
        printResults("SRT", jobs);
    }

    // Round Robin Scheduling
    static void roundRobin(List<Job> jobs, int quantum) {
        Queue<Job> queue = new LinkedList<>();
        List<Job> completed = new ArrayList<>();
        int time = 0;
        Set<Job> seen = new HashSet<>(); // Track jobs already added to queue
        while (completed.size() < jobs.size()) {
            for (Job job : jobs) {
                if (job.arrivalTime <= time && !seen.contains(job)) {
                    queue.offer(job); // Enqueue new arrivals
                    seen.add(job);
                }
            }
            if (queue.isEmpty()) {
                time++;
                continue;
            }
            Job current = queue.poll();
            int runTime = Math.min(quantum, current.remainingTime); // Time to run in this slice
            for (int t = 0; t < runTime; t++) {
                time++;
                for (Job job : jobs) {
                    if (job.arrivalTime == time && !seen.contains(job)) {
                        queue.offer(job);
                        seen.add(job);
                    }
                }
            }
            current.remainingTime -= runTime;
            if (current.remainingTime > 0) {
                queue.offer(current); // Not yet finished
            } else {
                current.turnaroundTime = time - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                completed.add(current);
            }
        }
        printResults("Round Robin", jobs);
    }

    // Main method to run all scheduling simulations
    public static void main(String[] args) {
        List<Job> originalJobs = createJobs(); // Initialize the job list

        // Run each scheduling algorithm with a fresh copy of the job list
        fcfs(deepCopy(originalJobs));
        sjn(deepCopy(originalJobs));
        srt(deepCopy(originalJobs));
        roundRobin(deepCopy(originalJobs), 4); // Round robin with quantum 4
    }
}
