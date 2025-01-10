using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;

namespace CloudWork.Filter
{
    public class TimerFilterAttribute : ActionFilterAttribute
    {
        private Stopwatch _timer;
        public override void OnActionExecuting(ActionExecutingContext context)
        {
            _timer = Stopwatch.StartNew();
        }

        public override void OnActionExecuted(ActionExecutedContext context)
        {
            _timer.Stop();
            var executionTime = _timer.Elapsed.TotalMilliseconds;

            if (context.Result is ViewResult viewResult && viewResult.ViewData != null)
            {
                viewResult.ViewData["ExecutionTime"] = $"{executionTime} ms";
            }
            context.HttpContext.Items["ExecutionTime"] = executionTime;

            base.OnActionExecuted(context);
        }
    }
}