using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;

namespace CloudWork.Filter
{
    public class TimerFilterAttribute : ActionFilterAttribute
    {
        private const string TimerKey = "Timer";
        public override void OnActionExecuting(ActionExecutingContext context)
        {
            var stopwatch = Stopwatch.StartNew();
            context.HttpContext.Items[TimerKey] = stopwatch;
        }

        public override void OnActionExecuted(ActionExecutedContext context)
        {
            if (context.HttpContext.Items.TryGetValue(TimerKey, out object timerObj) && timerObj is Stopwatch stopwatch)
            {
                stopwatch.Stop();
                var executionTime = stopwatch.Elapsed.TotalMilliseconds;
                context.HttpContext.Items["ExecutionTime"] = executionTime;
            }

            base.OnActionExecuted(context);
        }
    }
}