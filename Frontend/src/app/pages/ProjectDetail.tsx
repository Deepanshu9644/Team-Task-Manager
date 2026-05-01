import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { api, ProjectDetail, Task, ProjectMember, TaskStatus, TaskPriority } from "../services/api";
import { ArrowLeft, Plus, Users, CheckSquare, Trash2, Edit2, Calendar } from "lucide-react";
import { format } from "date-fns";
import { useAuth } from "../contexts/AuthContext";

export default function ProjectDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();
  const [project, setProject] = useState<ProjectDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [showCreateTask, setShowCreateTask] = useState(false);
  const [showInviteMember, setShowInviteMember] = useState(false);

  useEffect(() => {
    if (id) {
      loadProject();
    }
  }, [id]);

  const loadProject = async () => {
    try {
      const response = await api.get<ProjectDetail>(`/projects/${id}`);
      if (response.success && response.data) {
        setProject(response.data);
      }
    } catch (error) {
      console.error("Failed to load project:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProject = async () => {
    if (!confirm("Are you sure you want to delete this project?")) return;

    try {
      const response = await api.delete(`/projects/${id}`);
      if (response.success) {
        navigate("/projects");
      }
    } catch (error) {
      console.error("Failed to delete project:", error);
    }
  };

  const handleUpdateTaskStatus = async (taskId: number, status: TaskStatus) => {
    try {
      const response = await api.patch(`/projects/${id}/tasks/${taskId}/status`, {
        status,
      });
      if (response.success) {
        loadProject();
      }
    } catch (error) {
      console.error("Failed to update task status:", error);
    }
  };

  const handleDeleteTask = async (taskId: number) => {
    if (!confirm("Are you sure you want to delete this task?")) return;

    try {
      const response = await api.delete(`/projects/${id}/tasks/${taskId}`);
      if (response.success) {
        loadProject();
      }
    } catch (error) {
      console.error("Failed to delete task:", error);
    }
  };

  const handleRemoveMember = async (userId: number) => {
    if (!confirm("Are you sure you want to remove this member?")) return;

    try {
      const response = await api.delete(`/projects/${id}/members/${userId}`);
      if (response.success) {
        loadProject();
      }
    } catch (error) {
      console.error("Failed to remove member:", error);
    }
  };

  const handleLeaveProject = async () => {
    if (!confirm("Are you sure you want to leave this project?")) return;

    try {
      const response = await api.delete(`/projects/${id}/members/leave`);
      if (response.success) {
        navigate("/projects");
      }
    } catch (error) {
      console.error("Failed to leave project:", error);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-gray-600">Loading project...</div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-gray-600">Project not found</div>
      </div>
    );
  }

  const isProjectAdmin = user?.id === project.adminId;
  const tasksByStatus = {
    TODO: project.tasks.filter((t) => t.status === "TODO"),
    IN_PROGRESS: project.tasks.filter((t) => t.status === "IN_PROGRESS"),
    IN_REVIEW: project.tasks.filter((t) => t.status === "IN_REVIEW"),
    DONE: project.tasks.filter((t) => t.status === "DONE"),
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <button
          onClick={() => navigate("/projects")}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
        >
          <ArrowLeft className="w-5 h-5" />
          Back to Projects
        </button>

        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 mb-6">
          <div className="flex items-start justify-between mb-4">
            <div className="flex items-center gap-3">
              <div
                className="w-6 h-6 rounded-full"
                style={{ backgroundColor: project.color }}
              />
              <h1 className="text-3xl text-gray-900">{project.name}</h1>
            </div>
            <div className="flex gap-2">
              {isProjectAdmin && (
                <button
                  onClick={handleDeleteProject}
                  className="px-4 py-2 text-red-600 border border-red-300 rounded-lg hover:bg-red-50"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              )}
              {!isProjectAdmin && (
                <button
                  onClick={handleLeaveProject}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Leave Project
                </button>
              )}
            </div>
          </div>

          <p className="text-gray-600 mb-4">{project.description}</p>

          <div className="flex items-center gap-4 text-sm text-gray-600">
            <span className="px-3 py-1 bg-gray-100 rounded-full">
              {project.status}
            </span>
            <span className="flex items-center gap-1">
              <Calendar className="w-4 h-4" />
              Due: {format(new Date(project.dueDate), "MMM dd, yyyy")}
            </span>
            <span className="flex items-center gap-1">
              <Users className="w-4 h-4" />
              {project.members.length} members
            </span>
            <span className="flex items-center gap-1">
              <CheckSquare className="w-4 h-4" />
              {project.tasks.length} tasks
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          <div className="lg:col-span-2">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl text-gray-900">Tasks</h2>
              <button
                onClick={() => setShowCreateTask(true)}
                className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700"
              >
                <Plus className="w-5 h-5" />
                New Task
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {(["TODO", "IN_PROGRESS", "IN_REVIEW", "DONE"] as TaskStatus[]).map(
                (status) => (
                  <div key={status} className="bg-white rounded-xl p-4 border border-gray-100">
                    <h3 className="text-sm text-gray-600 mb-3">
                      {status.replace("_", " ")} ({tasksByStatus[status].length})
                    </h3>
                    <div className="space-y-2">
                      {tasksByStatus[status].map((task) => (
                        <TaskCard
                          key={task.id}
                          task={task}
                          onStatusChange={handleUpdateTaskStatus}
                          onDelete={handleDeleteTask}
                          isAdmin={isProjectAdmin}
                        />
                      ))}
                    </div>
                  </div>
                )
              )}
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl text-gray-900">Members</h2>
              {isProjectAdmin && (
                <button
                  onClick={() => setShowInviteMember(true)}
                  className="flex items-center gap-2 bg-indigo-600 text-white px-3 py-2 rounded-lg hover:bg-indigo-700"
                >
                  <Plus className="w-4 h-4" />
                  Invite
                </button>
              )}
            </div>

            <div className="bg-white rounded-xl p-4 border border-gray-100">
              <div className="space-y-3">
                {project.members.map((member) => (
                  <div
                    key={member.userId}
                    className="flex items-center justify-between p-3 border border-gray-200 rounded-lg"
                  >
                    <div>
                      <p className="text-gray-900">{member.userName}</p>
                      <p className="text-sm text-gray-500">{member.userEmail}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs px-2 py-1 bg-gray-100 rounded">
                        {member.role}
                      </span>
                      {isProjectAdmin && member.userId !== project.adminId && (
                        <button
                          onClick={() => handleRemoveMember(member.userId)}
                          className="text-red-600 hover:text-red-700"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {showCreateTask && (
        <CreateTaskModal
          projectId={project.id}
          members={project.members}
          onClose={() => setShowCreateTask(false)}
          onSuccess={() => {
            setShowCreateTask(false);
            loadProject();
          }}
        />
      )}

      {showInviteMember && (
        <InviteMemberModal
          projectId={project.id}
          onClose={() => setShowInviteMember(false)}
          onSuccess={() => {
            setShowInviteMember(false);
            loadProject();
          }}
        />
      )}
    </div>
  );
}

function TaskCard({
  task,
  onStatusChange,
  onDelete,
  isAdmin,
}: {
  task: Task;
  onStatusChange: (taskId: number, status: TaskStatus) => void;
  onDelete: (taskId: number) => void;
  isAdmin: boolean;
}) {
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "HIGH":
        return "text-red-600";
      case "MEDIUM":
        return "text-orange-600";
      case "LOW":
        return "text-green-600";
      default:
        return "text-gray-600";
    }
  };

  return (
    <div className="p-3 border border-gray-200 rounded-lg hover:border-indigo-300 transition-all">
      <div className="flex items-start justify-between mb-2">
        <h4 className="text-sm text-gray-900">{task.title}</h4>
        {isAdmin && (
          <button
            onClick={() => onDelete(task.id)}
            className="text-red-600 hover:text-red-700"
          >
            <Trash2 className="w-3 h-3" />
          </button>
        )}
      </div>
      <p className="text-xs text-gray-600 mb-2 line-clamp-2">{task.description}</p>
      <div className="flex items-center justify-between text-xs mb-2">
        <span className={getPriorityColor(task.priority)}>{task.priority}</span>
        <span className="text-gray-500">
          {format(new Date(task.dueDate), "MMM dd")}
        </span>
      </div>
      <select
        value={task.status}
        onChange={(e) => onStatusChange(task.id, e.target.value as TaskStatus)}
        className="w-full text-xs px-2 py-1 border border-gray-300 rounded"
      >
        <option value="TODO">TODO</option>
        <option value="IN_PROGRESS">IN PROGRESS</option>
        <option value="IN_REVIEW">IN REVIEW</option>
        <option value="DONE">DONE</option>
      </select>
    </div>
  );
}

function CreateTaskModal({
  projectId,
  members,
  onClose,
  onSuccess,
}: {
  projectId: number;
  members: ProjectMember[];
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TaskPriority>("MEDIUM");
  const [dueDate, setDueDate] = useState("");
  const [assigneeId, setAssigneeId] = useState<number>(members[0]?.userId || 0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await api.post(`/projects/${projectId}/tasks`, {
        title,
        description,
        priority,
        dueDate,
        assigneeId,
      });

      if (response.success) {
        onSuccess();
      } else {
        setError(response.message || "Failed to create task");
      }
    } catch (err) {
      setError("Failed to create task");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl max-w-md w-full p-6">
        <h2 className="text-2xl mb-6 text-gray-900">Create New Task</h2>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="title" className="block text-sm mb-2 text-gray-700">
              Task Title
            </label>
            <input
              id="title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Task title"
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm mb-2 text-gray-700">
              Description
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Task description"
            />
          </div>

          <div>
            <label htmlFor="priority" className="block text-sm mb-2 text-gray-700">
              Priority
            </label>
            <select
              id="priority"
              value={priority}
              onChange={(e) => setPriority(e.target.value as TaskPriority)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div>
            <label htmlFor="assignee" className="block text-sm mb-2 text-gray-700">
              Assign To
            </label>
            <select
              id="assignee"
              value={assigneeId}
              onChange={(e) => setAssigneeId(Number(e.target.value))}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              {members.map((member) => (
                <option key={member.userId} value={member.userId}>
                  {member.userName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="dueDate" className="block text-sm mb-2 text-gray-700">
              Due Date
            </label>
            <input
              id="dueDate"
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 disabled:opacity-50"
            >
              {loading ? "Creating..." : "Create Task"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function InviteMemberModal({
  projectId,
  onClose,
  onSuccess,
}: {
  projectId: number;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<"ADMIN" | "MEMBER">("MEMBER");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await api.post(`/projects/${projectId}/members/invite`, {
        email,
        role,
      });

      if (response.success) {
        onSuccess();
      } else {
        setError(response.message || "Failed to invite member");
      }
    } catch (err) {
      setError("Failed to invite member");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl max-w-md w-full p-6">
        <h2 className="text-2xl mb-6 text-gray-900">Invite Member</h2>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm mb-2 text-gray-700">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="member@example.com"
            />
          </div>

          <div>
            <label htmlFor="role" className="block text-sm mb-2 text-gray-700">
              Role
            </label>
            <select
              id="role"
              value={role}
              onChange={(e) => setRole(e.target.value as "ADMIN" | "MEMBER")}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="MEMBER">Member</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 disabled:opacity-50"
            >
              {loading ? "Inviting..." : "Invite"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
