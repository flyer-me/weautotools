using CloudWork.Model;
using CloudWork.Repository.DB;
using CloudWork.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CloudWork.Controllers
{
    [Authorize]
    public class JudgeController : Controller
    {
        private readonly AppDbContext _dbContext;
        private readonly CodeExecutionService _codeExecutionService;

        public JudgeController(AppDbContext dbContext,CodeExecutionService codeExecutionService)
        {
            _dbContext = dbContext;
            _codeExecutionService = codeExecutionService;
        }

        [HttpPost]
        public async Task<IActionResult> SubmitCode(Submission submission)
        {
            var testCases = _dbContext.Questions.Find(submission.QuestionId)?.TestCases?.ToList();
            if (testCases == null)
            {
                return NotFound(new { success = false, message = "本题无测试用例" });
            }
            var result = await CodeExecutionService.JuidgeAsync(submission, testCases);

            // 根据执行结果判断是否正确

            // 存储评测结果，更新数据库
            var evaluation = new SubmissionEvaluation
            {
                Id = Guid.NewGuid().ToString(),
                Message = result.Message,
                IsPassed = result.IsPassed,
                ExecutionTime = result.ExecutionTime,
                SubmissionId = submission.Id
            };

            // 保存评测记录到数据库
            await _dbContext.SubmissionEvaluations.AddAsync(evaluation);
            await _dbContext.SaveChangesAsync();

            return Ok(new { success = true, result });
        }
    }
}
