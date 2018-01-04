/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

// It's assumed that Jenkins has been configured to implicitly load the vars/*.groovy libraries.
// Note that the version used is the one defined in Jenkins but it can be overridden as follows:
// @Library("XWiki@<branch, tag, sha1>") _
// See https://github.com/jenkinsci/workflow-cps-global-lib-plugin for details.

stage ('Platform Builds') {
  parallel(
    'main': {
      // Build, skipping quality checks so that the result of the build can be sent as fast as possible to the devs.
      // In addition, we want the generated artifacts to be deployed to our remote Maven repository so that developers
      // can benefit from them even though some quality checks have not yet passed. In // we start a build with the
      // quality profile that executes various quality checks.
      //
      // Note: We configure the snapshot extension repository in XWiki (-PsnapshotModules) in the generated
      // distributions to make it easy for developers to install snapshot extensions when they do manual tests.
      build(
        name: 'Main',
        profiles: 'legacy,integration-tests,office-tests,snapshotModules',
        properties: '-Dxwiki.checkstyle.skip=true -Dxwiki.surefire.captureconsole.skip=true -Dxwiki.revapi.skip=true'
      )

      // Note: if an error occurs in the first build above, then an exception will be raised and this job will not
      // execute which is what we want since failures can be test flickers for ex, and it could still be interesting to
      // get a distribution to test xwiki manually.

      // Build the distributions
      build(
        name: 'Distribution',
        profiles: 'legacy,integration-tests,office-tests,snapshotModules',
        pom: 'xwiki-platform-distribution/pom.xml'
      )

      // Building the various functional tests, after the distribution has been built successfully.

      // Build the Flavor Test POM, required for the pageobjects module below.
      buildFunctionalTest(
        name: 'Flavor Test - POM',
        pom: 'pom.xml'
      )

      // Build the Flavor Test PageObjects required by the functional test below that need an XWiki UI
      buildFunctionalTest(
        name: 'Flavor Test - PageObjects',
        pom: 'xwiki-platform-distribution-flavor-test-pageobjects/pom.xml'
      )

      // Now run all tests in parallel
      parallel(
        'flavor-test-ui': {
          // Run the Flavor UI tests
          buildFunctionalTest(
            name: 'Flavor Test - UI',
            pom: 'xwiki-platform-distribution-flavor-test-ui/pom.xml'
          )
        },
        'flavor-test-misc': {
          // Run the Flavor Misc tests
          buildFunctionalTest(
            name: 'Flavor Test - Misc',
            pom: 'xwiki-platform-distribution-flavor-test-misc/pom.xml'
          )
        },
        'flavor-test-storage': {
          // Run the Flavor Storage tests
          buildFunctionalTest(
            name: 'Flavor Test - Storage',
            pom: 'xwiki-platform-distribution-flavor-test-storage/pom.xml'
          )
        },
        'flavor-test-escaping': {
          // Run the Flavor Escaping tests
          buildFunctionalTest(
            name: 'Flavor Test - Escaping',
            pom: 'xwiki-platform-distribution-flavor-test-escaping/pom.xml'
          )
        },
        'flavor-test-selenium': {
          // Run the Flavor Selenium tests
          buildFunctionalTest(
            name: 'Flavor Test - Selenium',
            pom: 'xwiki-platform-distribution-flavor-test-selenium/pom.xml'
          )
        },
        'flavor-test-webstandards': {
          // Run the Flavor Webstandards tests
          // Note: -XX:ThreadStackSize=2048 is used to prevent a StackOverflowError error when using the HTML5 Nu
          // Validator (see https://bitbucket.org/sideshowbarker/vnu/issues/4/stackoverflowerror-error-when-running)
          buildFunctionalTest(
            name: 'Flavor Test - Webstandards',
            pom: 'xwiki-platform-distribution-flavor-test-webstandards/pom.xml',
            mavenOpts: '-Xmx2500m -Xms512m -XX:ThreadStackSize=2048'
          )
        }
      )
    },
    'testrelease': {
      // Simulate a release and verify all is fine, in preparation for the release day.
      build(
        name: 'TestRelease',
        goals: 'clean install',
        profiles: 'hsqldb,jetty,legacy,integration-tests,standalone,flavor-integration-tests,distribution',
        properties: '-DskipTests -DperformRelease=true -Dgpg.skip=true -Dxwiki.checkstyle.skip=true'
      )
    },
    'quality': {
      // Run the quality checks
      build(
        name: 'Quality',
        goals: 'clean install jacoco:report',
        profiles: 'quality,legacy'
      )
    }
  )
}

def build(map)
{
  node {
    xwikiBuild(map.name) {
      mavenOpts = map.mavenOpts ?: "-Xmx2500m -Xms512m"
      if (map.goals) {
        goals = map.goals
      }
      if (map.profiles) {
        profiles = map.profiles
      }
      if (map.properties) {
        properties = map.properties
      }
      if (map.pom) {
        pom = map.pom
      }
    }
  }
}

def buildFunctionalTest(map)
{
  def sharedPOMPrefix =
    'xwiki-platform-distribution/xwiki-platform-distribution-flavor/xwiki-platform-distribution-flavor-test'

  build(
    name: map.name,
    profiles: 'legacy,integration-tests,jetty,hsqldb,firefox',
    mavenOpts: map.mavenOpts,
    pom: "${sharedPOMPrefix}/${map.pom}"
  )
}
