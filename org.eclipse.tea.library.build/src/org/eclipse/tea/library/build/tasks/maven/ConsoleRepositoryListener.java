/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.tea.library.build.tasks.maven;

import java.io.PrintStream;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener extends AbstractRepositoryListener {

    private final PrintStream out;

    public ConsoleRepositoryListener(PrintStream out) {
        this.out = (out != null) ? out : System.out;
    }

    @Override
    public void artifactDeployed(RepositoryEvent event) {
        out.println("Deployed " + event.getArtifact() + " to " + event.getRepository());
    }

    @Override
    public void artifactDeploying(RepositoryEvent event) {
        out.println("Deploying " + event.getArtifact() + " to " + event.getRepository());
    }

    @Override
    public void artifactDescriptorInvalid(RepositoryEvent event) {
        out.println("Invalid artifact descriptor for " + event.getArtifact() + ": " + event.getException().getMessage());
    }

    @Override
    public void artifactDescriptorMissing(RepositoryEvent event) {
        out.println("Missing artifact descriptor for " + event.getArtifact());
    }

    @Override
    public void artifactInstalled(RepositoryEvent event) {
        out.println("Installed " + event.getArtifact() + " to " + event.getFile());
    }

    @Override
    public void artifactInstalling(RepositoryEvent event) {
        out.println("Installing " + event.getArtifact() + " to " + event.getFile());
    }

    @Override
    public void artifactResolved(RepositoryEvent event) {
        out.println("Resolved artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    @Override
    public void artifactDownloading(RepositoryEvent event) {
        out.println("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    @Override
    public void artifactDownloaded(RepositoryEvent event) {
        out.println("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    @Override
    public void artifactResolving(RepositoryEvent event) {
        out.println("Resolving artifact " + event.getArtifact());
    }

    @Override
    public void metadataDeployed(RepositoryEvent event) {
        out.println("Deployed " + event.getMetadata() + " to " + event.getRepository());
    }

    @Override
    public void metadataDeploying(RepositoryEvent event) {
        out.println("Deploying " + event.getMetadata() + " to " + event.getRepository());
    }

    @Override
    public void metadataInstalled(RepositoryEvent event) {
        out.println("Installed " + event.getMetadata() + " to " + event.getFile());
    }

    @Override
    public void metadataInstalling(RepositoryEvent event) {
        out.println("Installing " + event.getMetadata() + " to " + event.getFile());
    }

    @Override
    public void metadataInvalid(RepositoryEvent event) {
        out.println("Invalid metadata " + event.getMetadata());
    }

    @Override
    public void metadataResolved(RepositoryEvent event) {
        out.println("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
    }

    @Override
    public void metadataResolving(RepositoryEvent event) {
        out.println("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
    }

}