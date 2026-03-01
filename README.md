# Smart Bulk Image Processor

## 🚀 Overview

High-performance system for bulk image processing using **Spring Boot 3.2**, **Java 21**, and **Spring Batch**. This project demonstrates how to handle large datasets of images efficiently using parallelism and batch processing.

## 🛠 Key Technical Concepts

### 1. Parallelism (Multi-threading)

We leverage `ThreadPoolTaskExecutor` to process multiple images simultaneously. Instead of a sequential bottleneck, the workload is distributed across multiple CPU cores, making the processing significantly faster.

### 2. Batch Processing (Spring Batch)

The project follows the standard **Read-Process-Write** pattern:

- **Reader:** Fetches image files from the input directory.
- **Processor:** Applies image filters (e.g., Grayscale, Resize) using `ImageFilterService`.
- **Writer:** Handles the final output (saving processed images).
Work is executed in **Chunks**, ensuring the system remains stable even with thousands of images.

### 3. Clean Code & Architecture

- **SOLID Principles:** Each class has a single, clear responsibility.
- **Service Layer:** Pure image manipulation logic isolated from framework concerns.
- **Scalability:** Easily add new filters by extending `ImageFilterService`.

## 📂 Project Structure

- `batch/`: Job and Step configurations.
- `config/`: Async and Task Executor settings.
- `service/`: Image processing algorithms.

## ⚙️ How to Run

1. Ensure **Java 21** and **Maven** are installed.
2. Place your source images in the `input-images` folder.
3. Run: `mvn spring-boot:run`
4. Find your results in the `output-images` folder.

---
*Created for the Image Processing Team (Graduation Project).*
