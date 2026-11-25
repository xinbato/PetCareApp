const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function buildPetPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    name: "Milo",
    species: "Cho",
    breed: "Corgi",
    dateOfBirth: "01/01/2024",
    age: "2 tuoi",
    weight: "11.5 kg",
    weightKg: 11.5,
    gender: "Duc",
    iconRes: 1,
    photoUri: "",
    color: "Vang trang",
    notes: "Thich an ca hoi",
    statusText: "Khoe manh",
    statusDetail: "Can nang 11.5 kg",
    isFavorite: false,
    createdByAi: false,
    temperature: "",
    activity: "",
    diet: "",
    lastUpdated: "vua xong",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function buildVaccinationPayload(uid, petId, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    petId,
    petName: "Milo",
    title: "Vac-xin 5 benh",
    dateTime: "01/12/2026",
    status: "Sap toi",
    scheduledDateMillis: Date.now() + 86400000,
    completed: false,
    reminderEnabled: true,
    reminderDaysBefore: 2,
    notes: "Tiem nhac hang nam",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function buildMedicationPayload(uid, petId, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    petId,
    petName: "Milo",
    name: "Men tieu hoa",
    dosage: "1 goi",
    scheduleTime: "08:30 • moi ngay",
    quantity: "1 goi",
    status: "Dang dung",
    reminderHour: 8,
    reminderMinute: 30,
    endDateMillis: Date.now() + 86400000 * 7,
    lastTakenDateKey: "",
    active: true,
    reminderEnabled: true,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function buildWeightPayload(uid, petId, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    petId,
    petName: "Milo",
    weight: "11.5 kg",
    weightKg: 11.5,
    dateTime: "25/11/2026",
    recordedAtMillis: Date.now(),
    status: "On dinh",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function buildSchedulePayload(uid, petId = "", overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    title: "Tam cho be",
    description: "Cham soc",
    petId,
    petName: petId ? "Milo" : "",
    dateText: "26/11/2026",
    timeText: "09:00",
    timeOrDateLabel: "09:00",
    triggerAtMillis: Date.now() + 86400000,
    reminderEnabled: true,
    isCompleted: false,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

test("Unauthenticated users cannot read or write private user collections or list usernames", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("pets").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("pets").doc("pet_1").set(buildPetPayload(ALICE_UID))
  );
  await assertFails(unauthDb.collection("usernames").get());
});

test("Owner can CRUD pets, vaccinations, medications, weights, and schedules with schema validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const petRef = aliceDb.collection("users").doc(ALICE_UID).collection("pets").doc("pet-1");
  const vacRef = aliceDb.collection("users").doc(ALICE_UID).collection("vaccinations").doc("vac-1");
  const medRef = aliceDb.collection("users").doc(ALICE_UID).collection("medications").doc("med-1");
  const weightRef = aliceDb.collection("users").doc(ALICE_UID).collection("weightRecords").doc("w-1");
  const schedRef = aliceDb.collection("users").doc(ALICE_UID).collection("schedules").doc("s-1");

  await assertSucceeds(petRef.set(buildPetPayload(ALICE_UID)));
  await assertSucceeds(petRef.update({ weightKg: 12.2, weight: "12.2 kg", updatedAt: new Date(Date.now() - 500) }));

  // Reject future timestamps and orphan vaccinations
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("pets").doc("pet-bad")
      .set(buildPetPayload(ALICE_UID, { createdAt: new Date(Date.now() + 60000) }))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaccinations").doc("vac-orphan")
      .set(buildVaccinationPayload(ALICE_UID, "missing-pet"))
  );

  await assertSucceeds(vacRef.set(buildVaccinationPayload(ALICE_UID, "pet-1")));
  await assertSucceeds(medRef.set(buildMedicationPayload(ALICE_UID, "pet-1")));
  await assertSucceeds(weightRef.set(buildWeightPayload(ALICE_UID, "pet-1")));
  await assertSucceeds(schedRef.set(buildSchedulePayload(ALICE_UID, "")));

  await assertSucceeds(schedRef.delete());
  await assertSucceeds(weightRef.delete());
  await assertSucceeds(medRef.delete());
  await assertSucceeds(vacRef.delete());
  await assertSucceeds(petRef.delete());
});

test("Cross-user access and spoofing are denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).collection("pets").doc("bob-pet").set(buildPetPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("pets").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("pets").doc("bob-pet").delete());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("pets").doc("spoof").set(buildPetPayload(BOB_UID))
  );
});
