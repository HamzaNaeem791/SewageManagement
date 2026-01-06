const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

function requireString(value, fieldName) {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new HttpsError("invalid-argument", `${fieldName} is required`);
  }
  return value.trim();
}

exports.createWorkerUser = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const callerUid = request.auth.uid;
  const callerDoc = await admin.firestore().collection("users").doc(callerUid).get();

  if (!callerDoc.exists) {
    throw new HttpsError("permission-denied", "Admin profile not found");
  }

  const callerRole = callerDoc.data().role;
  if (callerRole !== "admin") {
    throw new HttpsError("permission-denied", "Only admins can create worker accounts");
  }

  const data = request.data || {};
  const name = requireString(data.name, "name");
  const email = requireString(data.email, "email");
  const password = requireString(data.password, "password");

  if (password.length < 6) {
    throw new HttpsError("invalid-argument", "password must be at least 6 characters");
  }

  const phoneNumber = typeof data.phoneNumber === "string" ? data.phoneNumber.trim() : "";
  const address = typeof data.address === "string" ? data.address.trim() : "";

  let userRecord;
  try {
    userRecord = await admin.auth().createUser({
      email,
      password,
      displayName: name,
    });
  } catch (e) {
    if (e && e.code === "auth/email-already-exists") {
      throw new HttpsError("already-exists", "Email already in use");
    }
    throw new HttpsError("internal", e?.message || "Failed to create user");
  }

  const userDoc = {
    userId: userRecord.uid,
    name,
    email,
    dob: "",
    phoneNumber,
    address,
    role: "worker",
  };

  await admin.firestore().collection("users").doc(userRecord.uid).set(userDoc, { merge: true });

  return {
    uid: userRecord.uid,
  };
});
