package com.rockthevote.grommet.testdata

import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher

class TestDispatcherProvider(
    dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}