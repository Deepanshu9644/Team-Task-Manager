import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, DashboardStats, Task } from "../services/api";
import { LayoutDashboard, FolderKanban, CheckSquare, AlertCircle, Calendar } from "lucide-react";
import { format } from "date-fns";

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const response = await api.get<DashboardStats>("/dashboard");
      if (response.success && response.data) {
        setStats(response.data);
      }
    } catch (error) {
      console.error("Failed to load dashboard:", error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "TODO":
        return "bg-gray-100 text-gray-700";
      case "IN_PROGRESS":
        return "bg-blue-100 text-blue-700";
      case "IN_REVIEW":
        return "bg-yellow-100 text-yellow-700";
      case "DONE":
        return "bg-green-100 text-green-700";
      default:
        return "bg-gray-100 text-gray-700";
    }
  };

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

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-gray-600">Loading dashboard...</div>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-gray-600">Failed to load dashboard</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex items-center gap-3 mb-8">
          <LayoutDashboard className="w-8 h-8 text-indigo-600" />
          <h1 className="text-3xl text-gray-900">Dashboard</h1>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Total Projects</p>
                <p className="text-3xl text-gray-900">{stats.totalProjects}</p>
              </div>
              <FolderKanban className="w-10 h-10 text-indigo-600" />
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Total Tasks</p>
                <p className="text-3xl text-gray-900">{stats.totalTasks}</p>
              </div>
              <CheckSquare className="w-10 h-10 text-blue-600" />
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Completed</p>
                <p className="text-3xl text-gray-900">{stats.completedTasks}</p>
              </div>
              <CheckSquare className="w-10 h-10 text-green-600" />
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Overdue</p>
                <p className="text-3xl text-gray-900">{stats.overdueTasks}</p>
              </div>
              <AlertCircle className="w-10 h-10 text-red-600" />
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <h2 className="text-xl mb-4 text-gray-900">Recent Projects</h2>
            <div className="space-y-3">
              {stats.recentProjects.length === 0 ? (
                <p className="text-gray-500 text-center py-8">No projects yet</p>
              ) : (
                stats.recentProjects.map((project) => (
                  <Link
                    key={project.id}
                    to={`/projects/${project.id}`}
                    className="block p-4 border border-gray-200 rounded-lg hover:border-indigo-300 hover:shadow-md transition-all"
                  >
                    <div className="flex items-center gap-3 mb-2">
                      <div
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: project.color }}
                      />
                      <h3 className="text-gray-900">{project.name}</h3>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{project.description}</p>
                    <div className="flex items-center justify-between text-xs text-gray-500">
                      <span className="px-2 py-1 bg-gray-100 rounded">
                        {project.status}
                      </span>
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {format(new Date(project.dueDate), "MMM dd, yyyy")}
                      </span>
                    </div>
                  </Link>
                ))
              )}
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <h2 className="text-xl mb-4 text-gray-900">My Tasks</h2>
            <div className="space-y-3">
              {stats.myTasks.length === 0 ? (
                <p className="text-gray-500 text-center py-8">No tasks assigned</p>
              ) : (
                stats.myTasks.slice(0, 5).map((task) => (
                  <div
                    key={task.id}
                    className="p-4 border border-gray-200 rounded-lg"
                  >
                    <div className="flex items-start justify-between mb-2">
                      <h3 className="text-gray-900">{task.title}</h3>
                      <span
                        className={`text-xs px-2 py-1 rounded ${getStatusColor(
                          task.status
                        )}`}
                      >
                        {task.status.replace("_", " ")}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{task.description}</p>
                    <div className="flex items-center justify-between text-xs">
                      <span className={getPriorityColor(task.priority)}>
                        {task.priority}
                      </span>
                      <span className="text-gray-500 flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {format(new Date(task.dueDate), "MMM dd")}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {stats.overduedTasks.length > 0 && (
          <div className="bg-red-50 border border-red-200 rounded-xl p-6">
            <h2 className="text-xl mb-4 text-red-900 flex items-center gap-2">
              <AlertCircle className="w-5 h-5" />
              Overdue Tasks
            </h2>
            <div className="space-y-3">
              {stats.overduedTasks.map((task) => (
                <div
                  key={task.id}
                  className="p-4 bg-white border border-red-200 rounded-lg"
                >
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="text-gray-900">{task.title}</h3>
                    <span
                      className={`text-xs px-2 py-1 rounded ${getStatusColor(
                        task.status
                      )}`}
                    >
                      {task.status.replace("_", " ")}
                    </span>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">{task.description}</p>
                  <div className="flex items-center justify-between text-xs">
                    <span className={getPriorityColor(task.priority)}>
                      {task.priority}
                    </span>
                    <span className="text-red-600 flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      {format(new Date(task.dueDate), "MMM dd, yyyy")}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
