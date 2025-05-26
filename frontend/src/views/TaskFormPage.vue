<template>
  <v-app>
    <!-- Header Section -->
    <v-app-bar color="primary" class="px-4">
      <div class="header-left">
        <img src="/logo.png" alt="FocusFlow Logo" class="header-logo" />
        <h1 class="header-title">FocusFlow</h1>
      </div>
      <v-spacer></v-spacer>
      <v-btn color="white" variant="text" @click="goBack">
        <v-icon>mdi-arrow-left</v-icon>
        Back
      </v-btn>
    </v-app-bar>

    <!-- Main Content Section -->
    <v-main>
      <v-container class="pa-6">
        <v-card class="mx-auto" max-width="700">
          <v-card-title class="text-h5 pa-4"> Create New Task </v-card-title>

          <v-card-text>
            <v-form @submit.prevent="submitTask" ref="form">
              <v-text-field
                v-model="task.title"
                label="Task Title"
                :rules="[(v) => !!v || 'Task title is required']"
                required
                class="mb-4"
                data-cy="task-title-input"
              ></v-text-field>

              <v-textarea
                v-model="task.shortDescription"
                label="Short Description"
                :rules="[(v) => !!v || 'Short description is required']"
                required
                rows="3"
                class="mb-4"
                data-cy="task-short-description-input"
              ></v-textarea>

              <v-textarea
                v-model="task.longDescription"
                label="Long Description (Optional)"
                rows="4"
                class="mb-4"
                data-cy="task-long-description-input"
              ></v-textarea>

              <v-row>
                <v-col cols="6">
                  <v-select
                    v-model="task.priority"
                    label="Priority"
                    :items="priorityOptions"
                    :rules="[(v) => !!v || 'Priority is required']"
                    required
                    class="mb-4"
                    data-cy="task-priority-select"
                  ></v-select>
                </v-col>
                <v-col cols="6">
                  <v-text-field
                    v-model="task.dueDate"
                    label="Due Date"
                    type="datetime-local"
                    :rules="[(v) => !!v || 'Due date is required']"
                    required
                    class="mb-4"
                    data-cy="task-due-date-input"
                  ></v-text-field>
                </v-col>
              </v-row>

              <v-card-actions class="pa-0">
                <v-spacer></v-spacer>
                <v-btn
                  color="grey-darken-1"
                  variant="text"
                  @click="goBack"
                  class="mr-2"
                  data-cy="cancel-task-button"
                >
                  Cancel
                </v-btn>
                <v-btn
                  color="primary"
                  type="submit"
                  :loading="loading"
                  data-cy="create-task-button"
                >
                  Create Task
                </v-btn>
              </v-card-actions>
            </v-form>
          </v-card-text>
        </v-card>

        <!-- Error Alert -->
        <v-alert
          v-if="error"
          type="error"
          class="mt-4"
          variant="tonal"
          closable
          @click:close="error = null"
        >
          {{ error }}
        </v-alert>

        <!-- Success Alert -->
        <v-snackbar
          v-model="showSuccess"
          color="success"
          timeout="3000"
          location="top"
        >
          Task created successfully!
        </v-snackbar>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "../services/api";
import { user } from "../services/auth";

const router = useRouter();
const form = ref(null);
const loading = ref(false);
const error = ref(null);
const showSuccess = ref(false);

const task = ref({
  title: "",
  shortDescription: "",
  longDescription: "",
  priority: "",
  dueDate: "",
});

// Priority options matching backend enum
const priorityOptions = [
  { title: "Low", value: "LOW" },
  { title: "Medium", value: "MID" },
  { title: "High", value: "HIGH" },
  { title: "Urgent", value: "URGENT" },
];

/**
 * Submit task creation form
 */
const submitTask = async () => {
  const { valid } = await form.value.validate();

  if (!valid) return;

  loading.value = true;
  error.value = null;

  try {
    // Prepare task data according to backend TaskDTO structure
    const taskData = {
      title: task.value.title,
      shortDescription: task.value.shortDescription,
      longDescription: task.value.longDescription || null,
      dueDate: task.value.dueDate,
      priority: task.value.priority,
      createdById: user.value?.id, // Required field from backend
    };

    const response = await api.post("/api/v1/tasks", taskData);

    if (response.data && response.data.success) {
      showSuccess.value = true;

      // Redirect after short delay
      setTimeout(() => {
        router.push("/");
      }, 1500);
    } else {
      throw new Error("Failed to create task");
    }
  } catch (err) {
    console.error("Task creation error:", err);
    error.value =
      err.response?.data?.message || "Failed to create task. Please try again.";
  } finally {
    loading.value = false;
  }
};

/**
 * Navigate back to task list
 */
const goBack = () => {
  router.push("/");
};
</script>

<style scoped>
/* Header styles */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo {
  height: 32px;
  width: auto;
}

.header-title {
  color: white;
  font-size: 20px;
  font-weight: 500;
  margin: 0;
  letter-spacing: 0.5px;
}

/* Card styling */
.v-card {
  border-radius: 12px;
}

/* Override Vuetify default styles */
:deep(.v-text-field .v-input__details) {
  padding-inline-start: 0;
}

:deep(.v-textarea .v-input__details) {
  padding-inline-start: 0;
}

:deep(.v-card-title) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}
</style>
