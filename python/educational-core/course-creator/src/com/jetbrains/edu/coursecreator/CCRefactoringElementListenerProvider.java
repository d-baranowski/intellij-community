/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.edu.coursecreator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import com.jetbrains.edu.learning.core.EduNames;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.courseFormat.Task;
import com.jetbrains.edu.learning.courseFormat.TaskFile;
import com.jetbrains.edu.coursecreator.actions.CCRunTestsAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CCRefactoringElementListenerProvider implements RefactoringElementListenerProvider {
  @Nullable
  @Override
  public RefactoringElementListener getListener(PsiElement element) {
    return new CCRenameListener(element);
  }


  static class CCRenameListener extends RefactoringElementAdapter {

    private String myElementName;

    public CCRenameListener(PsiElement element) {
      if (element instanceof PsiFile) {
        PsiFile psiFile = (PsiFile)element;
        myElementName = psiFile.getName();
      }
    }

    @Override
    protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
      if (newElement instanceof PsiFile && myElementName != null) {
        PsiFile psiFile = (PsiFile)newElement;
        tryToRenameTaskFile(psiFile, myElementName);
      }
    }

    private static void tryToRenameTaskFile(PsiFile file, String oldName) {
      final PsiDirectory taskDir = file.getContainingDirectory();
      final CCProjectService service = CCProjectService.getInstance(file.getProject());
      Course course = service.getCourse();
      if (course == null) {
        return;
      }
      if (taskDir == null || !taskDir.getName().contains(EduNames.TASK)) {
        return;
      }
      PsiDirectory lessonDir = taskDir.getParent();
      if (lessonDir == null || !lessonDir.getName().contains(EduNames.LESSON)) {
        return;
      }
      Lesson lesson = course.getLesson(lessonDir.getName());
      if (lesson == null) {
        return;
      }
      Task task = lesson.getTask(taskDir.getName());
      if (task == null) {
        return;
      }
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          CCRunTestsAction.clearTestEnvironment(taskDir.getVirtualFile(), taskDir.getProject());
        }
      });
      Map<String, TaskFile> taskFiles = task.getTaskFiles();
      TaskFile taskFile = task.getTaskFile(oldName);
      taskFiles.remove(oldName);
      taskFiles.put(file.getName(), taskFile);
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {

    }
  }
}
