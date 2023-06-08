# Mobile Application Development - Repo
The collection of labs and the exam project repos related to the course of Mobile Application Development at Politecnico di Torino, a.y. 2022-2023

## Group members
- Francesco Rosati
- Giuseppe Lazzara
- Mario Mastrandrea
- Michele Pistan

## EzSport - Group Project
`/project` contains the repository for the exam group project, consisting in the implementation of a **native Android App** for the reservation of sport centers' playgrounds and the management of matches between users

### Main Features
- Access the app by **Google Authentication**
- Collect all the **reservations** interesting for the **user**, both as the organizer and a participant
- Show the **actual playgrounds availabilities** *in real-time*, for each sport and for each time slot
- List all the registered **playgrounds** for the provided sport centers, and for each of them describe all the main info (name, sport, price, opening hours, address, etc.) as well as their available **equipments** 
- **Reserve a playground** for given timeslots and (eventually) some related sport equipments
- Manage and review your **reservation**, add it to your *Google Wallet* and add the event to your *Google Calendar*     
- Search and **invite** other users to play sending them **push notifications**
- Accept or reject an **invitation**, and be notified of other users' invitations responses 
- **Rate** and **Review** a playground after a game
- Manage your **profile**: adding your info, select your favourite **sports** and your corresponding **sport level** and collect **achievements** playing to new sports and playgrounds 

### Technical features
- App is provided with a **Front-end** native **Android** architecture, based on the **MVVM** paradigm and leveraging the main **Kotlin androidx** libraries
- **Layout** is managed with classic Android components (no Jetpack compose)
- The backbone is based on a Repository class interacting with the cloud **Firebase** platform
- Data is stored both in a NoSQL **Firestore** DB and in a Firebase storage slice
- **Notifications** are managed by a Firebase Messaging Service      
