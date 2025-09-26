package com.dailyrounds.quizapp.di

import android.content.Context
import com.dailyrounds.quizapp.repository.QuestionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuestionRepository(
        @ApplicationContext context: Context
    ): QuestionRepository {
        return QuestionRepository(context)
    }
}
