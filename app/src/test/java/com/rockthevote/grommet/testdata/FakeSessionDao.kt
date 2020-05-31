package com.rockthevote.grommet.testdata

import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.data.db.relationship.SessionWithRegistrations

class FakeSessionDao(
    private val getCurrentSessionResult: MutableList<Session?> = mutableListOf()
) : SessionDao {
    private val _updatedSessions = mutableListOf<Session>()
    val updatedSessions: List<Session> get() = _updatedSessions

    override fun getAll(): List<Session> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentSession() = getCurrentSessionResult[0]

    override fun getSessionWithRegistrations(): List<SessionWithRegistrations> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(vararg session: Session) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearAllSessionInfo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateSession(session: Session) {
        _updatedSessions.add(session)
    }

}