<template>
  <div class="task-list">
    <!-- Header -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <img src="/logo.png" alt="FocusFlow Logo" class="header-logo" />
          <h1 class="header-title">FocusFlow</h1>
        </div>
        <div class="header-right">
          <span class="user-email">{{ user?.email }}</span>
          <v-btn
            color="white"
            variant="text"
            prepend-icon="mdi-logout"
            @click="handleLogout"
            class="ml-4"
          >
            Logout
          </v-btn>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <v-container class="py-8">
      <!-- Title and Create Button -->
      <div class="d-flex justify-space-between align-center mb-6">
        <h2 class="text-h4">Tasks</h2>
        <v-btn
          id="create-task-btn"
          color="primary"
          variant="elevated"
          prepend-icon="mdi-plus"
          @click="createTask"
        >
          CREATE NEW TASK
        </v-btn>
      </div>

      <!-- Table Section -->
      <v-card>
        <v-data-table
          id="tasks-table"
          :headers="headers"
          :items="taskList"
          :loading="loading"
          :items-per-page="rowsPerPage"
          class="elevation-1"
        >
          <template v-slot:item="{ item }">
            <tr>
              <td>{{ item.title }}</td>
              <td>{{ item.shortDescription }}</td>
              <td>
                <v-chip
                  :color="getStatusColor(item.status)"
                  size="small"
                  variant="flat"
                >
                  {{ item.status }}
                </v-chip>
              </td>
              <td>
                <v-chip
                  :color="getPriorityColor(item.priority)"
                  size="small"
                  variant="flat"
                >
                  {{ item.priority }}
                </v-chip>
              </td>
              <td class="text-right">
                <div class="d-flex justify-end">
                  <v-btn
                    :id="`edit-task-${item.id}`"
                    color="primary"
                    variant="text"
                    density="comfortable"
                    icon="mdi-pencil"
                    class="mr-2"
                    @click="editTask(item)"
                  ></v-btn>
                  <v-btn
                    :id="`delete-task-${item.id}`"
                    color="error"
                    variant="text"
                    density="comfortable"
                    icon="mdi-delete"
                    @click="deleteTask(item.id)"
                  ></v-btn>
                </div>
              </td>
            </tr>
          </template>

          <!-- No Data Template -->
          <template v-slot:no-data>
            <div id="no-data-message" class="pa-4 text-center">
              No data available
            </div>
          </template>
        </v-data-table>
      </v-card>

      <!-- Info Message -->
      <v-alert
        id="no-tasks-alert"
        v-if="taskList.length === 0 && !loading"
        type="info"
        class="mt-4"
        variant="tonal"
        border="start"
      >
        No tasks available at the moment.
      </v-alert>

      <!-- Backend Issue Alert -->
      <v-alert
        v-if="backendIssue"
        type="warning"
        class="mt-4"
        variant="tonal"
        border="start"
        closable
        @click:close="backendIssue = false"
      >
        <v-alert-title>Backend Database Issue</v-alert-title>
        There's a temporary database configuration issue on the backend. Task
        creation works, but task listing is currently unavailable. The
        development team is working on resolving this.
      </v-alert>

      <!-- Edit Dialog -->
      <v-dialog v-model="editDialog" max-width="600px">
        <v-card>
          <v-card-title class="text-h5 pa-4"> Edit Task </v-card-title>

          <v-card-text>
            <v-form ref="editForm" id="edit-task-form">
              <v-text-field
                id="edit-task-title"
                v-model="editedTask.title"
                label="Task Title"
                variant="outlined"
                density="comfortable"
                :rules="[(v) => !!v || 'Task title is required']"
                required
                class="mb-4"
                data-cy="edit-task-title-input"
              ></v-text-field>

              <v-textarea
                id="edit-task-description"
                v-model="editedTask.shortDescription"
                label="Short Description"
                variant="outlined"
                density="comfortable"
                :rules="[(v) => !!v || 'Description is required']"
                required
                rows="4"
                data-cy="edit-task-description-input"
              ></v-textarea>
            </v-form>
          </v-card-text>

          <v-card-actions class="pa-4 pt-0">
            <v-spacer></v-spacer>
            <v-btn
              id="cancel-edit-btn"
              color="grey-darken-1"
              variant="text"
              @click="closeEditDialog"
            >
              Cancel
            </v-btn>
            <v-btn
              id="save-edit-btn"
              color="primary"
              variant="elevated"
              :loading="updating"
              @click="updateTask"
            >
              Save
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "../services/api";
import { authService, user } from "../services/auth";

const router = useRouter();
const taskList = ref([]);
const loading = ref(false);
const rowsPerPage = ref(10);
const backendIssue = ref(false);

const headers = [
  { title: "Title", key: "title", align: "start" },
  { title: "Description", key: "shortDescription", align: "start" },
  { title: "Status", key: "status", align: "start" },
  { title: "Priority", key: "priority", align: "start" },
  { title: "Actions", key: "actions", align: "end", sortable: false },
];

/**
 * Get color for task status
 */
const getStatusColor = (status) => {
  const colors = {
    OPEN: "blue",
    IN_PROGRESS: "orange",
    REVIEW: "purple",
    BLOCKED: "red",
    CLOSED: "green",
  };
  return colors[status] || "grey";
};

/**
 * Get color for task priority
 */
const getPriorityColor = (priority) => {
  const colors = {
    LOW: "green",
    MID: "orange",
    HIGH: "red",
    URGENT: "deep-purple",
  };
  return colors[priority] || "grey";
};

/**
 * Fetch tasks from the backend
 */
const fetchTasks = async () => {
  loading.value = true;
  try {
    // Use the creator endpoint to get tasks created by the current user
    // This avoids the problematic search query in the backend
    const response = await api.get(
      `/api/v1/tasks/creator/${user.value?.id}?includeTags=false`
    );

    // Handle the ApiResponse wrapper structure
    if (response.data && response.data.success && response.data.data) {
      taskList.value = Array.isArray(response.data.data)
        ? response.data.data
        : [];
    } else {
      taskList.value = [];
    }
  } catch (error) {
    console.error("Error fetching tasks:", error);

    // If there's a backend database error, show empty list for now
    // This allows the frontend to work while backend issues are resolved
    taskList.value = [];

    // You could also show a user-friendly message here
    if (
      error.response?.data?.message?.includes("LazyInitializationException") ||
      error.response?.data?.message?.includes("operator does not exist")
    ) {
      console.warn(
        "Backend database configuration issue detected. Showing empty task list."
      );
      backendIssue.value = true;
    }
  } finally {
    loading.value = false;
  }
};

/**
 * Navigate to create task page
 */
const createTask = () => {
  router.push("/task/create");
};

/**
 * Delete a task
 */
const deleteTask = async (taskId) => {
  try {
    await api.delete(`/api/v1/tasks/${taskId}`);
    await fetchTasks();
  } catch (error) {
    console.error("Error deleting task:", error);
  }
};

/**
 * Handle user logout
 */
const handleLogout = () => {
  authService.logout();
  router.push("/login");
};

// Edit dialog state
const editDialog = ref(false);
const editForm = ref(null);
const updating = ref(false);
const editedTask = ref({
  id: null,
  title: "",
  shortDescription: "",
});

/**
 * Open edit dialog for a task
 */
const editTask = (task) => {
  editedTask.value = {
    id: task.id,
    title: task.title,
    shortDescription: task.shortDescription,
  };
  editDialog.value = true;
};

/**
 * Close edit dialog
 */
const closeEditDialog = () => {
  editDialog.value = false;
  editedTask.value = {
    id: null,
    title: "",
    shortDescription: "",
  };
};

/**
 * Update a task
 */
const updateTask = async () => {
  const { valid } = await editForm.value.validate();

  if (!valid) return;

  updating.value = true;
  try {
    await api.put(`/api/v1/tasks/${editedTask.value.id}`, {
      title: editedTask.value.title,
      shortDescription: editedTask.value.shortDescription,
    });
    await fetchTasks();
    closeEditDialog();
  } catch (error) {
    console.error("Error updating task:", error);
  } finally {
    updating.value = false;
  }
};

onMounted(() => {
  fetchTasks();
});
</script>

<style scoped>
.task-list {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.app-header {
  background-color: #009688; /* Teal color from logo */
  padding: 16px 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-logo {
  height: 32px;
  width: auto;
}

.header-title {
  color: white;
  font-size: 24px;
  font-weight: 500;
  margin: 0;
  letter-spacing: 0.5px;
}

.user-email {
  color: white;
  font-size: 14px;
  opacity: 0.9;
}

:deep(.v-data-table) {
  background-color: white;
}

:deep(.v-data-table-header th) {
  background-color: #f5f5f5 !important;
  color: rgba(0, 0, 0, 0.87) !important;
  font-size: 0.875rem;
  font-weight: 500 !important;
  text-transform: none !important;
}

:deep(.v-data-table-row td) {
  height: 48px;
}

:deep(.v-btn--icon) {
  margin: 0 2px;
}

.edit-dialog-form {
  padding: 20px;
}
</style>
