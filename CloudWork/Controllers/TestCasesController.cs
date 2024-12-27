using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using CloudWork.Repository.UnitOfWorks;
using CloudWork.Model;
using CloudWork.Service.Interface;
using CloudWork.Repository.Base;

namespace CloudWork.Web.Controllers
{
    public class TestCasesController : Controller
    {
        private readonly ITestCaseService _testCaseService;
        private readonly IBaseRepository<Question> _questionRepository;

        public TestCasesController(ITestCaseService testCaseService, IBaseRepository<Question> questionRepository)
        {
            _testCaseService = testCaseService;
            _questionRepository = questionRepository;
        }

        // GET: TestCases 默认需要导航属性
        public async Task<IActionResult> Index()
        {
            return View(await _testCaseService.GetTestCasesWithQuestionAsync());
        }

        // GET: TestCases/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _testCaseService.GetTestCaseWithQuestionAsync(id.Value);

            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // GET: TestCases/Create
        public async Task<IActionResult> Create()
        {
            var questions = await _questionRepository.GetAllAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title));
            return View();
        }

        // POST: TestCases/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,QuestionId,Input,ExpectedOutput")] TestCase testCase)
        {
            if (ModelState.IsValid)
            {
                try
                {
                    await _testCaseService.AddAsync(testCase);
                    return RedirectToAction(nameof(Index));
                }
                catch (Exception)
                {
                    // _unitOfWork.RollbackTransaction();
                    throw;
                }
            }
            var questions = await _questionRepository.GetAllAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _testCaseService.GetTestCaseWithQuestionAsync(id.Value);
            if (testCase == null)
            {
                return NotFound();
            }
            var questions = await _questionRepository.GetAllAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // POST: TestCases/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("Id,QuestionId,Input,ExpectedOutput")] TestCase testCase)
        {
            if (id != testCase.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    await _testCaseService.UpdateAsync(testCase);
                    return RedirectToAction(nameof(Index));
                }
                catch (DbUpdateConcurrencyException)
                {
                    var test = await _testCaseService.GetByIdAsync(id);
                    if (test == null)
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
            }
            var questions = await _questionRepository.GetAllAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _testCaseService.GetTestCaseWithQuestionAsync(id.Value);
            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // POST: TestCases/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var testCase = await _testCaseService.GetTestCaseWithQuestionAsync(id);
            if (testCase != null)
            {
                try
                {
                    await _testCaseService.DeleteAsync(id);
                }
                catch (Exception)
                {
                    throw;
                }
            }
            return RedirectToAction(nameof(Index));

        }
    }
}
