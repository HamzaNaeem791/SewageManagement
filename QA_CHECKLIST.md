# QA checklist

## Citizen
- Register creates a citizen account (role=citizen)
- Login routes to Citizen dashboard
- Submit complaint works and appears in History
- Tracking screen opens from History and shows timeline + map

## Admin
- Admin login routes to Admin dashboard
- Admin can create worker account (name/email/password) without being logged out
- New worker appears in Assign list
- Assign complaint to worker sets assignedTo and status to In Progress
- Update status (Pending/In Progress/Resolved) works

## Worker
- Worker login routes to Worker dashboard
- Worker sees only assigned jobs
- Worker can update status (In Progress/Resolved)

## Session / routing
- When logged out, Admin/Worker dashboards redirect to Login
- Splash routes correctly for all roles

## Security rules (after deploying firestore.rules)
- Citizen cannot read all complaints
- Worker cannot read unassigned complaints
- Citizen cannot change their role in users/{uid}
- Worker cannot reassign a complaint or change its userId
