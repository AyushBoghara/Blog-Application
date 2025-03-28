# Blog Application 📱

## Introduction
Welcome to the **Blog Application**, an innovative and user-centric mobile solution designed to revolutionize content management for bloggers and readers. This Android-based application enables users to create, manage, and interact with blog posts seamlessly. Built with **Android Studio** and integrated with **Firebase**, the application provides real-time content updates and a user-friendly experience.

## Features 🚀
### For User 
- **User Registration & Authentication** (Powered by Firebase Authentication)
- **Create, Edit, and Delete Blog Posts**
- **Real-time Synchronization** with Firestore Database
- **Profile Management**
- **Secure Content Management**
- **Browse & Explore Blog Posts**
- **Like and Comment on Posts**
- **Follow Favorite Bloggers**
- **Real-time Content Updates**

### For Admins 🛠️
- **Manage User Profiles**
- **Approve or Remove Blog Posts**
- **Monitor User Engagement**

## Technology Stack 🛠️
| Technology | Purpose |
|------------|---------|
| **Android Studio** | Official IDE for Android development |
| **Java** | Primary programming language for app development |
| **XML** | UI Layout designing |
| **Firebase Authentication** | Secure user authentication |
| **Firestore Database** | NoSQL database for real-time data storage |
| **Firebase Console** | Web-based interface to manage Firebase services |
| **Android SDK** | Essential libraries and tools for Android development |

## Database Structure 🗄️
The application uses **Firebase Firestore** to store and manage data efficiently.

### 1. Users Collection 👥
| Attribute | Data Type | Constraints |
|-----------|----------|------------|
| `userId` | Integer | Primary Key |
| `email` | String | Not Null |
| `userName` | String | Not Null |
| `user_image` | String | Optional |
| `password` | String | Not Null |

### 2. Articles Collection 📝
| Attribute | Data Type | Constraints |
|-----------|----------|------------|
| `articleId` | Integer | Primary Key |
| `articleTitle` | String | Not Null |
| `articleImage` | String | Not Null |
| `articleDescription` | String | Not Null |
| `articleCurrentDate` | Timestamp | Not Null |

### 3. Likes Collection ❤️
| Attribute | Data Type | Constraints |
|-----------|----------|------------|
| `like_id` | Integer | Primary Key |
| `articleId` | Integer | Foreign Key |
| `date` | Timestamp | Not Null |

### 4. Bookmarks Collection 📌
| Attribute | Data Type | Constraints |
|-----------|----------|------------|
| `bookMarkId` | Integer | Primary Key |
| `articleId` | Integer | Foreign Key |
| `date` | Timestamp | Not Null |

## Installation & Setup 🔧
Follow these steps to set up and run the Blog Application:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/AyushBoghara/Blog-Application.git
   cd blog-application
   ```

2. **Open in Android Studio**
   - Open **Android Studio**
   - Select **Open an existing project**
   - Choose the cloned repository

3. **Set Up Firebase**
   - Create a new project in **Firebase Console**
   - Enable **Authentication** (Email & Password)
   - Configure **Firestore Database**
   - Download `google-services.json` and place it in `app/` folder

4. **Build & Run the Application**
   - Connect an emulator or Android device
   - Click **Run ▶️** in Android Studio

## License 📜
This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---
🌟 **Developed by Aayush M. Boghara** 🌟
