using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using CloudWork.Data;
using CloudWork.Models;
using CloudWork.Repository;

namespace CloudWork.Controllers
{
    public class TestCasesController : Controller
    {
        private readonly CloudWorkDbContext _context;
        private readonly IGenericRepository<TestCase> _repository;
        private readonly ITestCaseRepository _testCaseRepository;

        public TestCasesController(CloudWorkDbContext context,
            ITestCaseRepository testCaseRepository, IGenericRepository<TestCase> repository)
        {
            _context = context;
            _repository = repository;
            _testCaseRepository = testCaseRepository;
        }

        // GET: TestCases 需要导航属性
        public async Task<IActionResult> Index()
        {
            return View(await _testCaseRepository.GetAllTestCasesAsync());
        }

        // GET: TestCases/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _testCaseRepository.GetTestCaseByIdAsync(id.Value);

            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // GET: TestCases/Create
        public IActionResult Create()
        {
            ViewData["QuestionId"] = new SelectList(_context.Questions, "Id", nameof(Question.Title));
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
                await _repository.AddAsync(testCase);
                await _repository.SaveAsync();
                return RedirectToAction(nameof(Index));
            }

            ViewData["QuestionId"] = new SelectList(_context.Questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await  _repository.GetByIdAsync(id.Value);
            if (testCase == null)
            {
                return NotFound();
            }
            ViewData["QuestionId"] = new SelectList(_context.Questions, "Id", nameof(Question.Title), testCase.QuestionId);
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
                    _repository.Update(testCase);
                    await _repository.SaveAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    var test = await _repository.GetByIdAsync(id);
                    if (test == null)
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["QuestionId"] = new SelectList(_context.Questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _testCaseRepository.GetTestCaseByIdAsync(id.Value);
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
            var testCase = await _repository.GetByIdAsync(id);
            if (testCase != null)
            {
                await _repository.DeleteAsync(id);
                await _repository.SaveAsync();
            }

            return RedirectToAction(nameof(Index));
        }

    }
}
