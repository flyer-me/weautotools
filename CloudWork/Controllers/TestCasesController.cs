using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using CloudWork.Data;
using CloudWork.Models;

namespace CloudWork.Controllers
{
    public class TestCasesController : Controller
    {
        private readonly CloudWorkDbContext _context;

        public TestCasesController(CloudWorkDbContext context)
        {
            _context = context;
        }

        // GET: TestCases
        public async Task<IActionResult> Index()
        {
            var cloudWorkDbContext = _context.TestCases.Include(t => t.Question);
            return View(await cloudWorkDbContext.ToListAsync());
        }

        // GET: TestCases/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _context.TestCases
                .Include(t => t.Question)
                .FirstOrDefaultAsync(m => m.Id == id);
            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // GET: TestCases/Create
        public IActionResult Create()
        {
            ViewData["QuestionId"] = new SelectList(_context.Questions, "Id", "Title");
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
                _context.Add(testCase);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }

            ViewData["QuestionId"] = new SelectList(_context.Questions, "Id", "Title", testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _context.TestCases.FindAsync(id);
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
                    _context.Update(testCase);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!TestCaseExists(testCase.Id))
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

            var testCase = await _context.TestCases
                .Include(t => t.Question)
                .FirstOrDefaultAsync(m => m.Id == id);
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
            var testCase = await _context.TestCases.FindAsync(id);
            if (testCase != null)
            {
                _context.TestCases.Remove(testCase);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool TestCaseExists(int id)
        {
            return _context.TestCases.Any(e => e.Id == id);
        }
    }
}
