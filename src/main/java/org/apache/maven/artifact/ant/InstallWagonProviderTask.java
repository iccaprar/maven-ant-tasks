package org.apache.maven.artifact.ant;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.tools.ant.BuildException;
import org.codehaus.plexus.PlexusContainerException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Ant Wrapper for wagon provider installation.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class InstallWagonProviderTask
    extends AbstractArtifactTask
{
    private String artifactId;

    private String version;

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public void execute()
        throws BuildException
    {
        try
        {
            MavenMetadataSource metadataSource = (MavenMetadataSource) lookup( ArtifactMetadataSource.ROLE );

            ArtifactResolver resolver = (ArtifactResolver) lookup( ArtifactResolver.ROLE );
            ArtifactRepository artifactRepository = createRemoteArtifactRepository( getDefaultRemoteRepository() );
            List remoteRepositories = Collections.singletonList( artifactRepository );
            try
            {
                ArtifactFactory factory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
                VersionRange versionRange = VersionRange.createFromVersionSpec( version );
                Artifact providerArtifact = factory.createExtensionArtifact( "org.apache.maven.wagon", artifactId,
                                                                             versionRange );
                ArtifactResolutionResult result = resolver.resolveTransitively( Collections
                    .singleton( providerArtifact ), createArtifact( createDummyPom() ),
                                                                                createLocalArtifactRepository(),
                                                                                remoteRepositories, metadataSource,
                                                                                null );

                log( "Installing provider: " + providerArtifact );

                for ( Iterator i = result.getArtifacts().iterator(); i.hasNext(); )
                {
                    Artifact a = (Artifact) i.next();
                    getEmbedder().getContainer().addJarResource( a.getFile() );
                }
            }
            catch ( ArtifactResolutionException e )
            {
                throw new BuildException( "Unable to locate wagon provider in remote repository", e );
            }
            catch ( PlexusContainerException e )
            {
                throw new BuildException( "Unable to locate wagon provider in remote repository", e );
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new BuildException( "Unable to locate wagon provider in remote repository", e );
            }
            catch ( ArtifactNotFoundException e )
            {
                throw new BuildException( "Unable to locate wagon provider in remote repository", e );
            }
        }
        catch ( BuildException e )
        {
            diagnoseError( e );
            
            throw e;
        }

    }
}
