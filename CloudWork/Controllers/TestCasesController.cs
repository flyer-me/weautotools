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
using CloudWork.Repository.Base;
using CloudWork.Repository.UnitOfWorks;

namespace CloudWork.Controllers
{
    public class TestCasesController : Controller
    {
        private readonly IUnitOfWork _unitOfWork;

        public TestCasesController(IUnitOfWork unitOfWork)
        {
            _unitOfWork = unitOfWork;
        }

        // GET: TestCases 默认需要导航属性
        public async Task<IActionResult> Index()
        {
            return View(await _unitOfWork.TestCases.GetAllTestCasesAsync());
        }

        // GET: TestCases/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _unitOfWork.TestCases.GetTestCaseByIdAsync(id.Value);

            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // GET: TestCases/Create
        public async Task<IActionResult> Create()
        {
            var questions = await _unitOfWork.Repository<Question>().GetAllAsync();
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
                    await _unitOfWork.TestCases.AddAsync(testCase);
                    await _unitOfWork.SaveAsync();
                    return RedirectToAction(nameof(Index));
                }
                catch (Exception)
                {
                    // _unitOfWork.RollbackTransaction();
                    throw;
                }
            }
            var questions = await _unitOfWork.Repository<Question>().GetAllAsync();
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

            var testCase = await _unitOfWork.TestCases.GetByIdAsync(id.Value);
            if (testCase == null)
            {
                return NotFound();
            }
            var questions = await _unitOfWork.Repository<Question>().GetAllAsync();
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
                    _unitOfWork.TestCases.Update(testCase);
                    await _unitOfWork.SaveAsync();
                    return RedirectToAction(nameof(Index));
                }
                catch (DbUpdateConcurrencyException)
                {
                    var test = await _unitOfWork.TestCases.GetByIdAsync(id);
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
            var questions = await _unitOfWork.Repository<Question>().GetAllAsync();
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

            var testCase = await _unitOfWork.TestCases.GetTestCaseByIdAsync(id.Value);
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
            var testCase = await _unitOfWork.TestCases.GetByIdAsync(id);
            if (testCase != null)
            {
                try
                {
                    await _unitOfWork.TestCases.DeleteAsync(id);
                    await _unitOfWork.SaveAsync();
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
