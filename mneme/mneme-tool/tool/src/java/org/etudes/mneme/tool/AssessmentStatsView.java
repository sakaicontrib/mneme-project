/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
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
 *
 **********************************************************************************/

package org.etudes.mneme.tool;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Web;

/**
 * The /assessment_stats view for the mneme tool.
 */
public class AssessmentStatsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentStatsView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Submission Service */
	protected SubmissionService submissionService = null;

	/** Dependency: ToolManager */
	protected ToolManager toolManager = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;


	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		String subId;
		
		// [2]sort for /grades, [3]aid
		if (params.length < 4 || params.length > 5) throw new IllegalArgumentException();

		// grades sort parameter
		String gradesSortCode = params[2];
		context.put("sort_grades", gradesSortCode);

		if (params.length == 5)
		{
			subId = params[4];
			context.put("submissionId", subId);
			Submission submission = this.submissionService.getSubmission(subId);
			context.put("submission", submission.getBest());
		}
		
		Assessment assessment = this.assessmentService.getAssessment(params[3]);
		if (assessment == null)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		if (this.submissionService.allowEvaluate(assessment.getContext()))
		{
			context.put("allowEval", Boolean.TRUE);
			context.put("grading", Boolean.TRUE);
		}
		else
		{
			context.put("allowEval", Boolean.FALSE);
		}


		// check that the assessment is not a formal course evaluation
		if (assessment.getFormalCourseEval())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// check that if a survey, the assessment has been frozen
		if ((assessment.getType() == AssessmentType.survey) && (!assessment.getFrozen()))
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// validity check
		if (!assessment.getIsValid())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		context.put("assessment", assessment);

		// collect all the submissions for the assessment
		List<Submission> submissions = this.submissionService.findAssessmentSubmissions(assessment,
				SubmissionService.FindAssessmentSubmissionsSort.sdate_a, Boolean.TRUE, null, null, null, null);
		context.put("submissions", submissions);

		computePercentComplete(assessment, submissions, context);

		String userId = sessionManager.getCurrentSessionUserId();
		context.put("currentUserId", userId);

		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2]sort for /grades, [3]aid
		if (params.length < 4 || params.length > 5) throw new IllegalArgumentException();
		// read form
		String destination = this.uiService.decode(req, context);

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * @param assessmentService
	 *        the assessmentService to set
	 */
	public void setAssessmentService(AssessmentService assessmentService)
	{
		this.assessmentService = assessmentService;
	}

	/**
	 * @param submissionService
	 *        the submissionService to set
	 */
	public void setSubmissionService(SubmissionService submissionService)
	{
		this.submissionService = submissionService;
	}

	/**
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		sessionManager = service;
	}

	/**
	 * Compute the percent complete for the set of submission.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param submissions
	 *        The submissions.
	 * @param context
	 *        The context.
	 */
	protected static void computePercentComplete(Assessment assessment, List<Submission> submissions, Context context)
	{
		Set<String> users = new HashSet<String>();
		int complete = 0;
		for (Submission s : submissions)
		{
			// if we have never seen this user before
			if (!users.contains(s.getUserId()))
			{
				// if non-phantom and complete, count it
				if ((!s.getIsPhantom()) && s.getIsComplete())
				{
					complete++;
				}

				users.add(s.getUserId());
			}
		}

		// percent
		int pct = users.isEmpty() ? 0 : ((complete * 100) / users.size());

		context.put("complete-percent", Integer.valueOf(pct));
		context.put("complete-complete", Integer.valueOf(complete));
		context.put("complete-total", Integer.valueOf(users.size()));
	}
}
