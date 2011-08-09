/*
 * Copyright 2011 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocpsoft.rewrite.servlet.config;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import com.ocpsoft.rewrite.EvaluationContext;
import com.ocpsoft.rewrite.config.Operation;
import com.ocpsoft.rewrite.servlet.config.parameters.ParameterBinding;
import com.ocpsoft.rewrite.servlet.config.parameters.ParameterizedOperation;
import com.ocpsoft.rewrite.servlet.config.parameters.impl.OperationParameterBuilder;
import com.ocpsoft.rewrite.servlet.config.parameters.impl.ParameterizedExpression;
import com.ocpsoft.rewrite.servlet.http.event.HttpInboundServletRewrite;
import com.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

/**
 * An {@link Operation} that performs redirects via {@link HttpInboundServletRewrite#redirectPermanent(String)} and
 * {@link HttpInboundServletRewrite#redirectTemporary(String)}
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Redirect extends HttpOperation implements ParameterizedOperation<OperationParameterBuilder>
{
   private final RedirectType type;

   private final ParameterizedExpression location;

   private Redirect(final String location, final RedirectType type)
   {
      this.location = new ParameterizedExpression(location);
      this.type = type;
   }

   @Override
   public void performHttp(final HttpServletRewrite event, final EvaluationContext context)
   {
      if (event instanceof HttpInboundServletRewrite)
      {
         String target = location.build(event, context);
         switch (type)
         {
         case PERMANENT:
            ((HttpInboundServletRewrite) event).redirectPermanent(target);
            break;
         case TEMPORARY:
            ((HttpInboundServletRewrite) event).redirectTemporary(target);
            break;
         default:
            break;
         }
      }
   }

   /**
    * Issue a permanent redirect ( 301 {@link HttpServletResponse#SC_MOVED_PERMANENTLY} ) to the given location. If the
    * given location is not the same as {@link HttpServletRewrite#getURL()}, this will change the browser {@link URL}
    * and result in a new request.
    * <p>
    * The given location may be parameterized using the following format:
    * <p>
    * <code>
    *    /example/{param} <br>
    *    /example/{value}/sub/{value2} <br>
    *    ... and so on
    * </code>
    * <p>
    * By default, matching parameter values are bound to the {@link EvaluationContext}.
    * <p>
    * See also {@link #where(String)}
    */
   public static Redirect permanent(final String location)
   {
      return new Redirect(location, RedirectType.PERMANENT);
   }

   /**
    * Issue a temporary redirect ( 302 {@link HttpServletResponse#SC_MOVED_TEMPORARILY} ) to the given location. If the
    * given location is not the same as {@link HttpServletRewrite#getURL()}, this will change the browser {@link URL}
    * and result in a new request.
    * <p>
    * The given location may be parameterized using the following format:
    * <p>
    * <code>
    *    /example/{param} <br>
    *    /example/{value}/sub/{value2} <br>
    *    ... and so on
    * </code>
    * <p>
    * By default, matching parameter values are bound to the {@link EvaluationContext}.
    * <p>
    * See also {@link #where(String)}
    */
   public static Redirect temporary(final String location)
   {
      return new Redirect(location, RedirectType.TEMPORARY);
   }

   private enum RedirectType
   {
      /**
       * 301
       */
      PERMANENT,
      /**
       * 302
       */
      TEMPORARY
   }

   @Override
   public OperationParameterBuilder where(final String param)
   {
      return new OperationParameterBuilder(this, location.getParameter(param));
   }

   @Override
   public OperationParameterBuilder where(final String param, final String pattern)
   {
      return where(param).matches(pattern);
   }

   @Override
   public OperationParameterBuilder where(final String param, final String pattern,
            final ParameterBinding binding)
   {
      return where(param, pattern).bindsTo(binding);
   }

   @Override
   public OperationParameterBuilder where(final String param, final ParameterBinding binding)
   {
      return where(param).bindsTo(binding);
   }

}
