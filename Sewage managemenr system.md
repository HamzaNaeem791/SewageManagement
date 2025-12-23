# **SINGLE APP – ROLE-BASED FLOW (Firebase Only)**

## **🎭 Roles Inside One App**

* **Citizen (User)** – Reports complaints

* **Worker** – Resolves assigned complaints

* **Admin / Authority** – Manages & governs system

All roles:

* Use the **same APK**

* Use the **same Firebase project**

* See **different screens & actions**

---

## **🔐 APP ENTRY FLOW (COMMON FOR ALL)**

### **1️⃣ App Launch**

1. Splash Screen

2. Firebase Authentication check

3. Fetch user profile from Firestore

4. Read `role` field

### **2️⃣ Role Router (Critical Screen)**

Based on role, user is routed to:

* **Citizen Home**

* **Worker Home**

* **Admin Dashboard**

➡ This routing happens **every app launch**

---

## **👤 CITIZEN (USER) FLOW**

### **Home Screen**

* New Complaint

* My Complaints

* Profile

* Notifications

### **Complaint Creation Flow**

1. Select issue category

2. Add description

3. Attach images

4. Pick location on map

5. Submit complaint

**System Behavior**

* Status \= `PENDING`

* Priority auto-set or user-selected

* Admin notified

---

### **My Complaints Flow**

* List of submitted complaints

* Real-time status updates

* Timeline view:

  * Submitted

  * Assigned

  * In Progress

  * Resolved / Escalated

---

## **👷 WORKER FLOW**

### **Worker Home**

* Assigned Complaints

* Map View

* Status Updates

* Notifications

### **Assigned Complaint Flow**

1. Worker opens complaint

2. Views:

   * Location

   * Images

   * Priority

3. Updates progress:

   * IN\_PROGRESS

   * RESOLVED

**System Behavior**

* Citizen receives updates

* Admin dashboard updates instantly

---

## **🧑‍💼 ADMIN / AUTHORITY FLOW**

### **Admin Dashboard**

* All Complaints (real-time)

* Filters:

  * Status

  * Priority

  * Area

  * Category

### **Admin Actions**

* Assign complaint to worker

* Update priority

* Override status

* Escalate complaint

* View analytics

---

### **Admin Map View**

* Clustered complaint markers

* Heatmap of problem areas

* Identify high-frequency zones

---

## **🔔 NOTIFICATIONS FLOW (ALL ROLES)**

### **Events That Trigger Notifications**

* Complaint assigned

* Status updated

* Escalation triggered

* Complaint resolved

**Delivery**

* Firebase Cloud Messaging

* In-app notification center

---

## **⚠️ ESCALATION FLOW (AUTOMATED)**

### **Escalation Rules**

* Complaint unresolved for X days

* Automatically escalated

### **Escalation Behavior**

* Status → ESCALATED

* Priority → HIGH

* Admin notified

* Highlighted in dashboard

---

## **📊 ANALYTICS FLOW (ADMIN ONLY)**

### **Dashboards Show**

* Total complaints

* Open vs resolved

* Escalation count

* Resolution time trends

* Area-based statistics

**Usage**

* Performance monitoring

* Reporting

* Resource planning

---

## **🗺 MAP-CENTRIC FLOW (ALL ROLES)**

### **Citizen**

* See own complaints on map

### **Worker**

* See assigned complaints on map

### **Admin**

* See all complaints with clustering

* Identify hotspots

---

## **🌙 DARK MODE (GLOBAL)**

* Follows system setting

* Applies across all roles

* No role-specific behavior

---

## **🔄 REAL-TIME SYSTEM FLOW (OVERVIEW)**

`Citizen Submits Complaint`  
          `↓`  
`Admin Views in Dashboard`  
          `↓`  
`Admin Assigns to Worker`  
          `↓`  
`Worker Updates Status`  
          `↓`  
`Citizen Receives Updates`  
          `↓`  
`Resolved OR Escalated`

---

## **🧠 WHY SINGLE APP WORKS BEST**

✔ One codebase  
 ✔ Easier updates  
 ✔ Role-based UI switching  
 ✔ Same Firebase backend  
 ✔ Faster development & testing  
 ✔ Ideal for government demos & academic projects

