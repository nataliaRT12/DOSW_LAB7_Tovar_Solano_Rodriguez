import { useCallback, useEffect, useState } from 'react'
import * as taskApi from '../../../api/taskApi'
import { useAuth } from '../../auth/context/AuthContext'

export function useTasks() {
  const { token, expireSession } = useAuth()
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleError = useCallback(
    (err) => {
      if (err.status === 401) {
        expireSession()
      } else {
        setError(err.message)
      }
    },
    [expireSession],
  )

  const loadTasks = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setTasks(await taskApi.getTasks(token))
    } catch (err) {
      handleError(err)
    } finally {
      setLoading(false)
    }
  }, [token, handleError])

  const addTask = useCallback(
    async (task) => {
      try {
        await taskApi.createTask(token, task)
        await loadTasks()
      } catch (err) {
        handleError(err)
      }
    },
    [token, loadTasks, handleError],
  )

  const editTask = useCallback(
    async (id, task) => {
      try {
        await taskApi.updateTask(token, id, task)
        await loadTasks()
      } catch (err) {
        handleError(err)
      }
    },
    [token, loadTasks, handleError],
  )

  const removeTask = useCallback(
    async (id) => {
      try {
        await taskApi.deleteTask(token, id)
        await loadTasks()
      } catch (err) {
        handleError(err)
      }
    },
    [token, loadTasks, handleError],
  )

  useEffect(() => {
    loadTasks()
  }, [loadTasks])

  return { tasks, loading, error, loadTasks, addTask, editTask, removeTask }
}
